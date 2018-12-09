﻿namespace luavm.binchunk
{
    struct binaryChunk
    {
        header header;
        byte sizeUpvalues;
        Prototype mainFunc;
    }

    struct header
    {
        byte[] signature;
        byte version;
        byte format;
        byte[] luacData;
        byte cintSize;
        byte sizetSize;
        byte instructionSize;
        byte luaIntegerSize;
        byte luaNumberSize;
        long luacInt;
        double luacNum;
    }

    public struct Prototype
    {
        internal string Source;
        internal uint LineDefined;
        internal uint LastLineDefined;
        internal byte NumParams;
        internal byte IsVararg;
        internal byte MaxStackSize;
        internal uint[] Code;
        internal object[] Constants;
        internal Upvalue[] Upvalues;
        internal Prototype[] Protos;
        internal uint[] LineInfo;
        internal LocVar[] LocVars;
        internal string[] UpvalueNames;
    }

    internal struct Upvalue
    {
        internal byte Instack;
        internal byte Idx;
    }

    internal struct LocVar
    {
        internal string VarName;
        internal uint StartPC;
        internal uint EndPC;
    }


    internal static class BinaryChunk
    {
        public const string LUA_SIGNATURE = "\x1bLua";
        public const byte LUAC_VERSION = 0x53;
        public const byte LUAC_FORMAT = 0;
        public const string LUAC_DATA = "\x19\x93\r\n\x1a\n";
        public const uint CINT_SIZE = 4;
        public const uint CSIZET_SIZE_32 = 4;
        public const uint CSIZET_SIZE_64 = 8;
        public const uint INSTRUCTION_SIZE = 4;
        public const uint LUA_INTEGER_SIZE = 8;
        public const uint LUA_NUMBER_SIZE = 8;
        public const ushort LUAC_INT = 0x5678;
        public const double LUAC_NUM = 370.5;

        public const byte TAG_NIL = 0x00;
        public const byte TAG_BOOLEAN = 0x01;
        public const byte TAG_NUMBER = 0x03;
        public const byte TAG_INTEGER = 0x13;
        public const byte TAG_SHORT_STR = 0x04;
        public const byte TAG_LONG_STR = 0x14;


        public static Prototype Undump(byte[] data)
        {
            var reader = new Reader {data = data};
            reader.checkHeader();
            reader.readByte();
            return reader.readProto("");
        }
    }
}