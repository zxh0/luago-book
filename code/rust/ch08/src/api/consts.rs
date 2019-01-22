/* basic types */
pub const LUA_TNONE: i8 = -1;
pub const LUA_TNIL: i8 = 0;
pub const LUA_TBOOLEAN: i8 = 1;
pub const LUA_TLIGHTUSERDATA: i8 = 2;
pub const LUA_TNUMBER: i8 = 3;
pub const LUA_TSTRING: i8 = 4;
pub const LUA_TTABLE: i8 = 5;
pub const LUA_TFUNCTION: i8 = 6;
pub const LUA_TUSERDATA: i8 = 7;
pub const LUA_TTHREAD: i8 = 8;

/* arithmetic functions */
pub const LUA_OPADD: u8 = 0; // +
pub const LUA_OPSUB: u8 = 1; // -
pub const LUA_OPMUL: u8 = 2; // *
pub const LUA_OPMOD: u8 = 3; // %
pub const LUA_OPPOW: u8 = 4; // ^
pub const LUA_OPDIV: u8 = 5; // /
pub const LUA_OPIDIV: u8 = 6; // //
pub const LUA_OPBAND: u8 = 7; // &
pub const LUA_OPBOR: u8 = 8; // |
pub const LUA_OPBXOR: u8 = 9; // ~
pub const LUA_OPSHL: u8 = 10; // <<
pub const LUA_OPSHR: u8 = 11; // >>
pub const LUA_OPUNM: u8 = 12; // -
pub const LUA_OPBNOT: u8 = 13; // ~

/* comparison functions */
pub const LUA_OPEQ: u8 = 0; // ==
pub const LUA_OPLT: u8 = 1; // <
pub const LUA_OPLE: u8 = 2; // <=
