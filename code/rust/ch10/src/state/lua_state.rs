use super::closure::Closure;
use super::lua_stack::LuaStack;
use super::lua_value::LuaValue;
use crate::api::{consts::*, LuaAPI, LuaVM, RustFn};
use crate::binary::chunk::Constant;
use crate::vm::instruction::Instruction;
use std::rc::Rc;

const LUA_RIDX_GLOBALS: LuaValue = LuaValue::Integer(crate::api::consts::LUA_RIDX_GLOBALS as i64);

pub struct LuaState {
    registry: LuaValue,
    frames: Vec<LuaStack>,
}

impl LuaState {
    pub fn new() -> LuaState {
        let registry = LuaValue::new_table(0, 0);
        if let LuaValue::Table(t) = &registry {
            let globals = LuaValue::new_table(0, 0);
            t.put(LUA_RIDX_GLOBALS, globals);
        }

        let fake_closure = Rc::new(Closure::new_fake_closure());
        let fake_frame = LuaStack::new(20, registry.clone(), fake_closure);
        LuaState {
            registry: registry,
            frames: vec![fake_frame],
        }
    }

    fn stack_mut(&mut self) -> &mut LuaStack {
        self.frames.last_mut().unwrap() // TODO
    }

    fn stack(&self) -> &LuaStack {
        self.frames.last().unwrap() // TODO
    }

    fn push_frame(&mut self, frame: LuaStack) {
        self.frames.push(frame);
    }

    fn pop_frame(&mut self) -> LuaStack {
        self.frames.pop().unwrap()
    }
}

impl LuaVM for LuaState {
    fn pc(&self) -> isize {
        self.stack().pc
    }

    fn add_pc(&mut self, n: isize) {
        self.stack_mut().pc += n;
    }

    fn fetch(&mut self) -> u32 {
        let instr = self.stack().closure.proto.code[self.stack().pc as usize];
        self.stack_mut().pc += 1;
        return instr;
    }

    fn get_const(&mut self, idx: isize) {
        let c = &self.stack().closure.proto.constants[idx as usize];
        let val = match c {
            Constant::Nil => LuaValue::Nil,
            Constant::Boolean(b) => LuaValue::Boolean(*b),
            Constant::Integer(i) => LuaValue::Integer(*i),
            Constant::Number(n) => LuaValue::Number(*n),
            Constant::Str(s) => LuaValue::Str((*s).clone()),
        };
        self.stack_mut().push(val);
    }

    fn get_rk(&mut self, rk: isize) {
        if rk > 0xFF {
            // constant
            self.get_const(rk & 0xFF);
        } else {
            // register
            self.push_value(rk + 1);
        }
    }

    fn register_count(&self) -> usize {
        self.stack().closure.proto.max_stack_size as usize
    }

    fn load_vararg(&mut self, mut n: isize) {
        if n < 0 {
            n = self.stack().varargs.len() as isize;
        }

        let varargs = self.stack().varargs.clone();
        self.stack_mut().check(n as usize);
        self.stack_mut().push_n(varargs, n);
    }

    fn load_proto(&mut self, idx: usize) {
        let proto = self.stack().closure.proto.protos[idx].clone();
        let closure = LuaValue::new_lua_closure(proto);
        self.stack_mut().push(closure);
    }
}

impl LuaAPI for LuaState {
    /* basic stack manipulation */

    fn get_top(&self) -> isize {
        self.stack().top()
    }

    fn abs_index(&self, idx: isize) -> isize {
        self.stack().abs_index(idx)
    }

    fn check_stack(&mut self, n: usize) -> bool {
        self.stack_mut().check(n);
        true
    }

    fn pop(&mut self, n: usize) {
        for _ in 0..n {
            self.stack_mut().pop();
        }
    }

    fn copy(&mut self, from_idx: isize, to_idx: isize) {
        let val = self.stack().get(from_idx);
        self.stack_mut().set(to_idx, val);
    }

    fn push_value(&mut self, idx: isize) {
        let val = self.stack().get(idx);
        self.stack_mut().push(val);
    }

    fn replace(&mut self, idx: isize) {
        let val = self.stack_mut().pop();
        self.stack_mut().set(idx, val);
    }

    fn insert(&mut self, idx: isize) {
        self.rotate(idx, 1);
    }

    fn remove(&mut self, idx: isize) {
        self.rotate(idx, -1);
        self.pop(1);
    }

    fn rotate(&mut self, idx: isize, n: isize) {
        let abs_idx = self.stack().abs_index(idx);
        if abs_idx < 0 || !self.stack().is_valid(abs_idx) {
            panic!("invalid index!");
        }

        let t = self.stack().top() - 1; /* end of stack segment being rotated */
        let p = abs_idx - 1; /* start of segment */
        let m = if n >= 0 { t - n } else { p - n - 1 }; /* end of prefix */
        self.stack_mut().reverse(p as usize, m as usize); /* reverse the prefix with length 'n' */
        self.stack_mut().reverse(m as usize + 1, t as usize); /* reverse the suffix */
        self.stack_mut().reverse(p as usize, t as usize); /* reverse the entire segment */
    }

    fn set_top(&mut self, idx: isize) {
        self.stack_mut().set_top(idx);
    }

    /* access functions (stack -> rust) */

    fn type_name(&self, tp: i8) -> &str {
        match tp {
            LUA_TNONE => "no value",
            LUA_TNIL => "nil",
            LUA_TBOOLEAN => "boolean",
            LUA_TNUMBER => "number",
            LUA_TSTRING => "string",
            LUA_TTABLE => "table",
            LUA_TFUNCTION => "function",
            LUA_TTHREAD => "thread",
            LUA_TLIGHTUSERDATA => "userdata",
            LUA_TUSERDATA => "userdata",
            _ => "?", // TODO
        }
    }

    fn type_id(&self, idx: isize) -> i8 {
        if self.stack().is_valid(idx) {
            self.stack().get(idx).type_id()
        } else {
            LUA_TNONE
        }
    }

    fn is_none(&self, idx: isize) -> bool {
        self.type_id(idx) == LUA_TNONE
    }

    fn is_nil(&self, idx: isize) -> bool {
        self.type_id(idx) == LUA_TNIL
    }

    fn is_none_or_nil(&self, idx: isize) -> bool {
        self.type_id(idx) <= LUA_TNIL
    }

    fn is_boolean(&self, idx: isize) -> bool {
        self.type_id(idx) == LUA_TBOOLEAN
    }

    fn is_table(&self, idx: isize) -> bool {
        self.type_id(idx) == LUA_TTABLE
    }

    fn is_function(&self, idx: isize) -> bool {
        self.type_id(idx) == LUA_TFUNCTION
    }

    fn is_thread(&self, idx: isize) -> bool {
        self.type_id(idx) == LUA_TTHREAD
    }

    fn is_string(&self, idx: isize) -> bool {
        let t = self.type_id(idx);
        t == LUA_TSTRING || t == LUA_TNUMBER
    }

    fn is_number(&self, idx: isize) -> bool {
        self.to_numberx(idx).is_some()
    }

    fn is_integer(&self, idx: isize) -> bool {
        match self.stack().get(idx) {
            LuaValue::Integer(_) => true,
            _ => false,
        }
    }

    fn is_rust_function(&self, idx: isize) -> bool {
        match self.stack().get(idx) {
            LuaValue::Function(c) => c.rust_fn.is_some(),
            _ => false,
        }
    }

    fn to_boolean(&self, idx: isize) -> bool {
        self.stack().get(idx).to_boolean()
    }

    fn to_integer(&self, idx: isize) -> i64 {
        self.to_integerx(idx).unwrap()
    }

    fn to_integerx(&self, idx: isize) -> Option<i64> {
        match self.stack().get(idx) {
            LuaValue::Integer(i) => Some(i),
            _ => None,
        }
    }

    fn to_number(&self, idx: isize) -> f64 {
        self.to_numberx(idx).unwrap()
    }

    fn to_numberx(&self, idx: isize) -> Option<f64> {
        match self.stack().get(idx) {
            LuaValue::Number(n) => Some(n),
            LuaValue::Integer(i) => Some(i as f64),
            _ => None,
        }
    }

    fn to_string(&self, idx: isize) -> String {
        self.to_stringx(idx).unwrap()
    }

    fn to_stringx(&self, idx: isize) -> Option<String> {
        match self.stack().get(idx) {
            LuaValue::Str(s) => Some(s),
            LuaValue::Number(n) => Some(n.to_string()),
            LuaValue::Integer(i) => Some(i.to_string()),
            _ => None,
        }
    }

    fn to_rust_function(&self, idx: isize) -> Option<RustFn> {
        match self.stack().get(idx) {
            LuaValue::Function(c) => c.rust_fn,
            _ => None,
        }
    }

    /* push functions (rust -> stack) */

    fn push_nil(&mut self) {
        self.stack_mut().push(LuaValue::Nil);
    }

    fn push_boolean(&mut self, b: bool) {
        self.stack_mut().push(LuaValue::Boolean(b));
    }

    fn push_integer(&mut self, n: i64) {
        self.stack_mut().push(LuaValue::Integer(n));
    }

    fn push_number(&mut self, n: f64) {
        self.stack_mut().push(LuaValue::Number(n));
    }

    fn push_string(&mut self, s: String) {
        self.stack_mut().push(LuaValue::Str(s));
    }

    fn push_rust_function(&mut self, f: RustFn) {
        self.stack_mut().push(LuaValue::new_rust_closure(f));
    }

    fn push_global_table(&mut self) {
        if let LuaValue::Table(t) = &self.registry {
            let global = t.get(&LUA_RIDX_GLOBALS);
            self.stack_mut().push(global);
        }
    }

    /* comparison and arithmetic functions */

    fn arith(&mut self, op: u8) {
        if op != LUA_OPUNM && op != LUA_OPBNOT {
            let b = self.stack_mut().pop();
            let a = self.stack_mut().pop();
            if let Some(result) = super::arith_ops::arith(&a, &b, op) {
                self.stack_mut().push(result);
                return;
            }
        } else {
            let a = self.stack_mut().pop();
            if let Some(result) = super::arith_ops::arith(&a, &a, op) {
                self.stack_mut().push(result);
                return;
            }
        }
        panic!("arithmetic error!");
    }

    fn compare(&self, idx1: isize, idx2: isize, op: u8) -> bool {
        if !self.stack().is_valid(idx1) || !self.stack().is_valid(idx2) {
            false
        } else {
            let a = self.stack().get(idx1);
            let b = self.stack().get(idx2);
            if let Some(result) = super::cmp_ops::compare(&a, &b, op) {
                return result;
            }
            panic!("comparison error!")
        }
    }

    /* miscellaneous functions */

    fn len(&mut self, idx: isize) {
        let _len = match self.stack().get(idx) {
            LuaValue::Str(s) => s.len(),
            LuaValue::Table(t) => t.len(),
            _ => panic!("length error!"),
        };
        self.stack_mut().push(LuaValue::Integer(_len as i64));
    }

    fn concat(&mut self, n: isize) {
        if n == 0 {
            self.stack_mut().push(LuaValue::Str(String::new()))
        } else if n >= 2 {
            for _ in 1..n {
                if self.is_string(-1) && self.is_string(-2) {
                    let s2 = self.to_string(-1);
                    let mut s1 = self.to_string(-2);
                    s1.push_str(&s2);
                    self.stack_mut().pop();
                    self.stack_mut().pop();
                    self.stack_mut().push(LuaValue::Str(s1));
                } else {
                    panic!("concatenation error!");
                }
            }
        }
        // n == 1, do nothing
    }

    /* get functions (Lua -> stack) */

    fn new_table(&mut self) {
        self.create_table(0, 0);
    }

    fn create_table(&mut self, narr: usize, nrec: usize) {
        self.stack_mut().push(LuaValue::new_table(narr, nrec));
    }

    fn get_table(&mut self, idx: isize) -> i8 {
        let t = self.stack().get(idx);
        let k = self.stack_mut().pop();
        self.get_table_impl(&t, &k)
    }

    fn get_field(&mut self, idx: isize, k: &str) -> i8 {
        let t = self.stack().get(idx);
        let k = LuaValue::Str(k.to_string()); // TODO
        self.get_table_impl(&t, &k)
    }

    fn get_i(&mut self, idx: isize, i: i64) -> i8 {
        let t = self.stack().get(idx);
        let k = LuaValue::Integer(i);
        self.get_table_impl(&t, &k)
    }

    fn get_global(&mut self, name: &str) -> i8 {
        if let LuaValue::Table(r) = &self.registry {
            let t = r.get(&LUA_RIDX_GLOBALS);
            let k = LuaValue::Str(name.to_string()); // TODO
            self.get_table_impl(&t, &k)
        } else {
            0
        }
    }

    /* set functions (stack -> Lua) */

    fn set_table(&mut self, idx: isize) {
        let t = self.stack().get(idx);
        let v = self.stack_mut().pop();
        let k = self.stack_mut().pop();
        LuaState::set_table_impl(&t, k, v);
    }

    fn set_field(&mut self, idx: isize, k: &str) {
        let t = self.stack().get(idx);
        let v = self.stack_mut().pop();
        let k = LuaValue::Str(k.to_string()); // TODO
        LuaState::set_table_impl(&t, k, v);
    }

    fn set_i(&mut self, idx: isize, i: i64) {
        let t = self.stack().get(idx);
        let v = self.stack_mut().pop();
        let k = LuaValue::Integer(i);
        LuaState::set_table_impl(&t, k, v);
    }

    fn set_global(&mut self, name: &str) {
        if let LuaValue::Table(r) = &self.registry {
            let t = r.get(&LUA_RIDX_GLOBALS);
            let v = self.stack_mut().pop();
            let k = LuaValue::Str(name.to_string()); // TODO
            LuaState::set_table_impl(&t, k, v);
        }
    }

    fn register(&mut self, name: &str, f: RustFn) {
        self.push_rust_function(f);
        self.set_global(name);
    }

    /* 'load' and 'call' functions (load and run Lua code) */

    fn load(&mut self, chunk: Vec<u8>, _chunk_name: &str, _mode: &str) -> u8 {
        let proto = crate::binary::undump(chunk);
        let c = LuaValue::new_lua_closure(proto);
        self.stack_mut().push(c);
        0 // TODO
    }

    fn call(&mut self, nargs: usize, nresults: isize) {
        let val = self.stack().get(-(nargs as isize + 1));
        if let LuaValue::Function(c) = val {
            if c.rust_fn.is_some() {
                self.call_rust_closure(nargs, nresults, c);
            } else {
                self.call_lua_closure(nargs, nresults, c);
            }
        } else {
            panic!("not function!");
        }
    }
}

impl LuaState {
    fn get_table_impl(&mut self, t: &LuaValue, k: &LuaValue) -> i8 {
        if let LuaValue::Table(tbl) = t {
            let v = tbl.get(k);
            let type_id = v.type_id();
            self.stack_mut().push(v);
            type_id
        } else {
            panic!("not a table!") // todo
        }
    }

    fn set_table_impl(t: &LuaValue, k: LuaValue, v: LuaValue) {
        if let LuaValue::Table(tbl) = t {
            tbl.put(k, v);
        } else {
            panic!("not a table!");
        }
    }

    fn call_rust_closure(&mut self, nargs: usize, nresults: isize, c: Rc<Closure>) {
        // create new lua stack
        let rust_fn = c.rust_fn.unwrap();
        let mut new_stack = LuaStack::new(nargs + LUA_MINSTACK, self.registry.clone(), c);

        // pass args, pop func
        if nargs > 0 {
            let args = self.stack_mut().pop_n(nargs);
            new_stack.push_n(args, nargs as isize);
        }
        self.stack_mut().pop(); // pop func

        // run closure
        self.push_frame(new_stack);
        let r = rust_fn(self);
        new_stack = self.pop_frame();

        // return results
        if nresults != 0 {
            let results = new_stack.pop_n(r);
            self.stack_mut().check(results.len());
            self.stack_mut().push_n(results, nresults);
        }
    }

    fn call_lua_closure(&mut self, nargs: usize, nresults: isize, c: Rc<Closure>) {
        let nregs = c.proto.max_stack_size as usize;
        let nparams = c.proto.num_params as usize;
        let is_vararg = c.proto.is_vararg == 1;

        // create new lua stack
        let mut new_stack = LuaStack::new(nregs + LUA_MINSTACK, self.registry.clone(), c);

        // pass args, pop func
        let mut args = self.stack_mut().pop_n(nargs);
        self.stack_mut().pop(); // pop func
        if nargs > nparams {
            // varargs
            for _ in nparams..nargs {
                new_stack.varargs.push(args.pop().unwrap());
            }
            if is_vararg {
                new_stack.varargs.reverse();
            } else {
                new_stack.varargs.clear();
            }
        }
        new_stack.push_n(args, nparams as isize);
        new_stack.set_top(nregs as isize);

        // run closure
        self.push_frame(new_stack);
        self.run_lua_closure();
        new_stack = self.pop_frame();

        // return results
        if nresults != 0 {
            let nrets = new_stack.top() as usize - nregs;
            let results = new_stack.pop_n(nrets);
            self.stack_mut().check(nrets);
            self.stack_mut().push_n(results, nresults);
        }
    }

    fn run_lua_closure(&mut self) {
        loop {
            let instr = self.fetch();
            instr.execute(self);
            // print_stack(instr.opname(), self);
            if instr.opcode() == crate::vm::opcodes::OP_RETURN {
                break;
            }
        }
    }
}
