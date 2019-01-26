use super::instr_call::*;
use super::instr_for::*;
use super::instr_load::*;
use super::instr_misc::*;
use super::instr_ops::*;
use super::instr_table::*;
use super::opcodes::*;
use crate::api::LuaVM;

const MAXARG_BX: isize = (1 << 18) - 1; // 262143
const MAXARG_SBX: isize = MAXARG_BX >> 1; // 131071

/*
 31       22       13       5    0
  +-------+^------+-^-----+-^-----
  |b=9bits |c=9bits |a=8bits|op=6|
  +-------+^------+-^-----+-^-----
  |    bx=18bits    |a=8bits|op=6|
  +-------+^------+-^-----+-^-----
  |   sbx=18bits    |a=8bits|op=6|
  +-------+^------+-^-----+-^-----
  |    ax=26bits            |op=6|
  +-------+^------+-^-----+-^-----
 31      23      15       7      0
*/
pub trait Instruction {
    fn opname(self) -> &'static str;
    fn opmode(self) -> u8;
    fn b_mode(self) -> u8;
    fn c_mode(self) -> u8;
    fn opcode(self) -> u8;
    fn abc(self) -> (isize, isize, isize);
    fn a_bx(self) -> (isize, isize);
    fn a_sbx(self) -> (isize, isize);
    fn ax(self) -> isize;
    fn execute(self, vm: &mut LuaVM);
}

impl Instruction for u32 {
    fn opname(self) -> &'static str {
        OPCODES[self.opcode() as usize].name
    }

    fn opmode(self) -> u8 {
        OPCODES[self.opcode() as usize].opmode
    }

    fn b_mode(self) -> u8 {
        OPCODES[self.opcode() as usize].bmode
    }

    fn c_mode(self) -> u8 {
        OPCODES[self.opcode() as usize].cmode
    }

    fn opcode(self) -> u8 {
        self as u8 & 0x3F
    }

    fn abc(self) -> (isize, isize, isize) {
        let a = (self >> 6 & 0xFF) as isize;
        let c = (self >> 14 & 0x1FF) as isize;
        let b = (self >> 23 & 0x1FF) as isize;
        (a, b, c)
    }

    fn a_bx(self) -> (isize, isize) {
        let a = (self >> 6 & 0xFF) as isize;
        let bx = (self >> 14) as isize;
        (a, bx)
    }

    fn a_sbx(self) -> (isize, isize) {
        let (a, bx) = self.a_bx();
        (a, bx - MAXARG_SBX)
    }

    fn ax(self) -> isize {
        (self >> 6) as isize
    }

    fn execute(self, vm: &mut LuaVM) {
        match self.opcode() {
            OP_MOVE => _move(self, vm),
            OP_LOADK => load_k(self, vm),
            OP_LOADKX => load_kx(self, vm),
            OP_LOADBOOL => load_bool(self, vm),
            OP_LOADNIL => load_nil(self, vm),
            // OP_GETUPVAL => (),
            // OP_GETTABUP => (),
            OP_GETTABLE => get_table(self, vm),
            // OP_SETTABUP => (),
            // OP_SETUPVAL => (),
            OP_SETTABLE => set_table(self, vm),
            OP_NEWTABLE => new_table(self, vm),
            OP_SELF => _self(self, vm),
            OP_ADD => add(self, vm),
            OP_SUB => sub(self, vm),
            OP_MUL => mul(self, vm),
            OP_MOD => _mod(self, vm),
            OP_POW => pow(self, vm),
            OP_DIV => div(self, vm),
            OP_IDIV => idiv(self, vm),
            OP_BAND => band(self, vm),
            OP_BOR => bor(self, vm),
            OP_BXOR => bxor(self, vm),
            OP_SHL => shl(self, vm),
            OP_SHR => shr(self, vm),
            OP_UNM => unm(self, vm),
            OP_BNOT => bnot(self, vm),
            OP_NOT => not(self, vm),
            OP_LEN => length(self, vm),
            OP_CONCAT => concat(self, vm),
            OP_JMP => jmp(self, vm),
            OP_EQ => eq(self, vm),
            OP_LT => lt(self, vm),
            OP_LE => le(self, vm),
            OP_TEST => test(self, vm),
            OP_TESTSET => test_set(self, vm),
            OP_CALL => call(self, vm),
            OP_TAILCALL => tail_call(self, vm),
            OP_RETURN => _return(self, vm),
            OP_FORLOOP => for_loop(self, vm),
            OP_FORPREP => for_prep(self, vm),
            // OP_TFORCALL => (),
            // OP_TFORLOOP => (),
            OP_SETLIST => set_list(self, vm),
            OP_CLOSURE => closure(self, vm),
            OP_VARARG => vararg(self, vm),
            // OP_EXTRAARG => (),
            _ => {
                dbg!(self.opname());
                unimplemented!()
            }
        }
    }
}
