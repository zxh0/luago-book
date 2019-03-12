using System;
using System.Linq;
using luavm.util;

namespace luavm.binchunk
{
    internal class Reader
    {
        public byte[] data;

        public byte readByte()
        {
            var b = data[0];
            data = data.Skip(1).ToArray();
            return b;
        }

        uint readUint32()
        {
            var bytes = new byte[4];
            Array.ConstrainedCopy(data, 0, bytes, 0, 4);
            data = data.Skip(4).ToArray();
            if (!BitConverter.IsLittleEndian)
            {
                Array.Reverse(bytes);
            }

            return BitConverter.ToUInt32(bytes, 0);
        }

        long readLuaInteger()
        {
            return (long) readUint64();
        }

        double readLuaNumber()
        {
            return BitConverter.Int64BitsToDouble((long) readUint64());
        }

        private ulong readUint64()
        {
            var bytes = new byte[8];
            Array.ConstrainedCopy(data, 0, bytes, 0, 8);
            data = data.Skip(8).ToArray();
            if (!BitConverter.IsLittleEndian)
            {
                Array.Reverse(bytes);
            }

            return BitConverter.ToUInt64(bytes, 0);
        }

        private byte[] readBytes(uint n)
        {
            var bytes = new byte[n];
            Array.ConstrainedCopy(data, 0, bytes, 0, (int) n);
            data = data.Skip((int) n).ToArray();
            return bytes;
        }

        public void checkHeader()
        {
//            Console.WriteLine(Encoding.Default.GetString(readBytes(4)));
            if (ConvertUtil.Bytes2String(readBytes(4)) != BinaryChunk.LUA_SIGNATURE)
            {
                Console.WriteLine("not a precompiled chunk!");
            }

            if (readByte() != BinaryChunk.LUAC_VERSION)
            {
                Console.WriteLine("version mismatch!");
            }

            if (readByte() != BinaryChunk.LUAC_FORMAT)
            {
                Console.WriteLine("format mismatch!");
            }

            if (ConvertUtil.Bytes2String(readBytes(6)) != BinaryChunk.LUAC_DATA)
            {
                Console.WriteLine("corrupted!");
            }

            if (readByte() != BinaryChunk.CINT_SIZE)
            {
                Console.WriteLine("int size mismatch!");
            }

            var b = readByte();
            if (b != BinaryChunk.CSIZET_SIZE_32 && b != BinaryChunk.CSIZET_SIZE_64)
            {
                Console.WriteLine("size_t size mismatch!");
            }

            if (readByte() != BinaryChunk.INSTRUCTION_SIZE)
            {
                Console.WriteLine("instruction size mismatch!");
            }

            if (readByte() != BinaryChunk.LUA_INTEGER_SIZE)
            {
                Console.WriteLine("lua_Integer size mismatch!");
            }

            if (readByte() != BinaryChunk.LUA_NUMBER_SIZE)
            {
                Console.WriteLine("lua_Number size mismatch!");
            }

            if (readLuaInteger() != BinaryChunk.LUAC_INT)
            {
                Console.WriteLine("endianness mismatch!");
            }

            if (!readLuaNumber().Equals(BinaryChunk.LUAC_NUM))
            {
                Console.WriteLine("float format mismatch!");
            }
        }

        public Prototype readProto(string parentSource)
        {
            var source = readString();
            if (source == "")
            {
                source = parentSource;
            }

            return new Prototype
            {
                Source = source,
                LineDefined = readUint32(),
                LastLineDefined = readUint32(),
                NumParams = readByte(),
                IsVararg = readByte(),
                MaxStackSize = readByte(),
                Code = readCode(),
                Constants = readConstants(),
                Upvalues = readUpvalues(),
                Protos = readProtos(source),
                LineInfo = readLineInfo(),
                LocVars = readLocVars(),
                UpvalueNames = readUpvalueNames()
            };
        }

        private string[] readUpvalueNames()
        {
            var names = new string[readUint32()];
            for (var i = 0; i < names.Length; i++)
            {
                names[i] = readString();
            }

            return names;
        }

        private LocVar[] readLocVars()
        {
            var locVars = new LocVar[readUint32()];
            for (var i = 0; i < locVars.Length; i++)
            {
                locVars[i] = new LocVar
                {
                    VarName = readString(),
                    StartPC = readUint32(),
                    EndPC = readUint32()
                };
            }

            return locVars;
        }

        private uint[] readLineInfo()
        {
            var lineInfo = new uint[readUint32()];
            for (var i = 0; i < lineInfo.Length; i++)
            {
                lineInfo[i] = readUint32();
            }

            return lineInfo;
        }

        private Prototype[] readProtos(string parentSource)
        {
            var protos = new Prototype[readUint32()];
            for (var i = 0; i < protos.Length; i++)
            {
                protos[i] = readProto(parentSource);
            }

            return protos;
        }

        private Upvalue[] readUpvalues()
        {
            var upvalues = new Upvalue[readUint32()];
            for (var i = 0; i < upvalues.Length; i++)
            {
                upvalues[i] = new Upvalue
                {
                    Instack = readByte(),
                    Idx = readByte()
                };
            }

            return upvalues;
        }

        private object[] readConstants()
        {
            var constants = new object[readUint32()];
            for (var i = 0; i < constants.Length; i++)
            {
                constants[i] = readConstant();
            }

            return constants;
        }

        private object readConstant()
        {
            switch (readByte())
            {
                case BinaryChunk.TAG_NIL: return null;
                case BinaryChunk.TAG_BOOLEAN: return readByte() != 0;
                case BinaryChunk.TAG_INTEGER: return readLuaInteger();
                case BinaryChunk.TAG_NUMBER: return readLuaNumber();
                case BinaryChunk.TAG_SHORT_STR: return readString();
                case BinaryChunk.TAG_LONG_STR: return readString();
                default: throw new Exception("corrupted!");
            }
        }

        private uint[] readCode()
        {
            var code = new uint[readUint32()];
            for (var i = 0; i < code.Length; i++)
            {
                code[i] = readUint32();
            }

            return code;
        }

        private string readString()
        {
            var size = (uint) readByte();
            if (size == 0)
            {
                return "";
            }

            if (size == 0xFF)
            {
                size = (uint) readUint64();
            }

            var bytes = readBytes(size - 1);
            return ConvertUtil.Bytes2String(bytes);
        }
    }
}