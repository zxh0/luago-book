using System;
using System.Linq;
using luavm.api;
using luavm.binchunk;
using luavm.vm;

namespace luavm.state
{
    public partial class LuaState
    {
        public int Load(ref byte[] chunk, string chunkName, string mode)
        {
            var proto = BinaryChunk.Undump(chunk);
            var c = Closure.NewLuaClosure(proto);
            _stack.Push(c);
            if (proto.Upvalues.Length > 0)
            {
                var env = Registry.Get(Constant.LUA_RIDX_GLOBALS);
                c.Upvals[0] = new Upvalue(env);
            }

            return 0;
        }

        public void Call(int nArgs, int nResults)
        {
            var val = _stack.Get(-(nArgs + 1));
            var f = val as Closure;
            if (f is null)
            {
                if (GetMetafield(val, "__call", this) is Closure mf)
                {
                    _stack.Push(null);
                    Insert(-(nArgs + 2));
                    nArgs += 1;
                    f = mf;
                }
            }

            if (f != null)
            {
                if (f.Proto != null)
                {
                    CallLuaClosure(nArgs, nResults, f);
                }
                else
                {
                    CallCsharpClosure(nArgs, nResults, f);
                }
            }
            else
            {
                throw new Exception("not function!");
            }
        }

        private void CallCsharpClosure(int nArgs, int nResults, Closure c)
        {
            // create new lua stack
            var newStack = new LuaStack {State = this, Closure = c};

            // pass args, pop func
            if (nArgs > 0)
            {
                newStack.PushN(_stack.PopN(nArgs), nArgs);
            }

            _stack.Pop();

            // run closure
            PushLuaStack(newStack);
            var r = c.CsharpFunc(this);
            PopLuaStack();

            // return results
            if (nResults != 0)
            {
                var results = newStack.PopN(r);
                //stack.check(results.Length);
                _stack.PushN(results, nResults);
            }
        }

        private void CallLuaClosure(int nArgs, int nResults, Closure c)
        {
            var nRegs = c.Proto.MaxStackSize;
            var nParams = c.Proto.NumParams;
            var isVararg = c.Proto.IsVararg == 1;

            // create new lua stack
            var newStack = new LuaStack {Closure = c};

            // pass args, pop func
            var funcAndArgs = _stack.PopN(nArgs + 1);
            var count = funcAndArgs.Count - 1;
            newStack.PushN(funcAndArgs.GetRange(1, count), nParams);
            if (nArgs > nParams && isVararg)
            {
                newStack.Varargs = funcAndArgs.GetRange(nParams + 1, count);
            }

            // run closure
            PushLuaStack(newStack);
            SetTop(nRegs);
            RunLuaClosure();
            PopLuaStack();

            // return results
            if (nResults != 0)
            {
                var results = newStack.PopN(newStack.Top() - nRegs);
                //stack.check(results.size())
                _stack.PushN(results, nResults);
            }
        }

        private void RunLuaClosure()
        {
            for (;;)
            {
                var inst = new Instruction(Fetch());
                var opCodeInt = inst.Opcode();
                var opcode = Constant.Opcodes[opCodeInt];
                
                //Console.WriteLine(opcode.Name);

                inst.Execute(opcode, this);

                if (opCodeInt == Constant.OP_RETURN)
                {
                    break;
                }
            }
        }
    }
}