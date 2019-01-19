use vm::opcodes::OPCODES;

const MAXARG_BX: i32 = 1 << 18 - 1; // 262143
const MAXARG_SBX: i32 = MAXARG_BX >> 1; // 131071

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
    fn opcode(self) -> i32;
    fn abc(self) -> (i32, i32, i32);
    fn a_bx(self) -> (i32, i32);
    fn a_sbx(self) -> (i32, i32);
    fn ax(self) -> i32;
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

    fn opcode(self) -> i32 {
        self as i32 & 0x3F
    }

    fn abc(self) -> (i32, i32, i32) {
        let a = (self >> 6 & 0xFF) as i32;
        let c = (self >> 14 & 0x1FF) as i32;
        let b = (self >> 23 & 0x1FF) as i32;
        (a, b, c)
    }

    fn a_bx(self) -> (i32, i32) {
        let a = (self >> 6 & 0xFF) as i32;
        let bx = (self >> 14) as i32;
        (a, bx)
    }

    fn a_sbx(self) -> (i32, i32) {
        let (a, bx) = self.a_bx();
        (a, bx - MAXARG_SBX)
    }

    fn ax(self) -> i32 {
        (self >> 6) as i32
    }
}
