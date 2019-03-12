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
                throw new Exception("not a precompiled chunk!");
            }

            if (readByte() != BinaryChunk.LUAC_VERSION)
            {
                throw new Exception("version mismatch!");
            }

            if (readByte() != BinaryChunk.LUAC_FORMAT)
            {
                throw new Exception("format mismatch!");
            }

            if (ConvertUtil.Bytes2String(readBytes(6)) != BinaryChunk.LUAC_DATA)
            {
                throw new Exception("corrupted!");
            }

            if (readByte() != BinaryChunk.CINT_SIZE)
            {
                throw new Exception("int size mismatch!");
            }

            var b = readByte();
            if (b != BinaryChunk.CSIZET_SIZE_32 && b != BinaryChunk.CSIZET_SIZE_64)
            {
                throw new Exception("size_t size mismatch!");
            }

            if (readByte() != BinaryChunk.INSTRUCTION_SIZE)
            {
                throw new Exception("instruction size mismatch!");
            }

            if (readByte() != BinaryChunk.LUA_INTEGER_SIZE)
            {
                throw new Exception("lua_Integer size mismatch!");
            }

            if (readByte() != BinaryChunk.LUA_NUMBER_SIZE)
            {
                throw new Exception("lua_Number size mismatch!");
            }

            if (readLuaInteger() != BinaryChunk.LUAC_INT)
            {
                throw new Exception("endianness mismatch!");
            }

            if (!readLuaNumber().Equals(BinaryChunk.LUAC_NUM))
            {
                throw new Exception("float format mismatch!");
            }
        }

        public Prototype readProto(string parentSource)
        {
            var source = ReadString();
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
                Code = ReadCode(),
                Constants = ReadConstants(),
                Upvalues = ReadUpvalues(),
                Protos = ReadProtos(source),
                LineInfo = ReadLineInfo(),
                LocVars = ReadLocVars(),
                UpvalueNames = ReadUpvalueNames()
            };
        }

        private string[] ReadUpvalueNames()
        {
            var names = new string[readUint32()];
            for (var i = 0; i < names.Length; i++)
            {
                names[i] = ReadString();
            }

            return names;
        }

        private LocVar[] ReadLocVars()
        {
            var locVars = new LocVar[readUint32()];
            for (var i = 0; i < locVars.Length; i++)
            {
                locVars[i] = new LocVar
                {
                    VarName = ReadString(),
                    StartPc = readUint32(),
                    EndPc = readUint32()
                };
            }

            return locVars;
        }

        private uint[] ReadLineInfo()
        {
            var lineInfo = new uint[readUint32()];
            for (var i = 0; i < lineInfo.Length; i++)
            {
                lineInfo[i] = readUint32();
            }

            return lineInfo;
        }

        private Prototype[] ReadProtos(string parentSource)
        {
            var protos = new Prototype[readUint32()];
            for (var i = 0; i < protos.Length; i++)
            {
                protos[i] = new Prototype();
                protos[i] = readProto(parentSource);
            }

            return protos;
        }

        private Upvalue[] ReadUpvalues()
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

        private object[] ReadConstants()
        {
            var constants = new object[readUint32()];
            for (var i = 0; i < constants.Length; i++)
            {
                constants[i] = ReadConstant();
            }

            return constants;
        }

        private object ReadConstant()
        {
            switch (readByte())
            {
                case BinaryChunk.TAG_NIL: return null;
                case BinaryChunk.TAG_BOOLEAN: return readByte() != 0;
                case BinaryChunk.TAG_INTEGER: return readLuaInteger();
                case BinaryChunk.TAG_NUMBER: return readLuaNumber();
                case BinaryChunk.TAG_SHORT_STR: return ReadString();
                case BinaryChunk.TAG_LONG_STR: return ReadString();
                default: throw new Exception("corrupted!");
            }
        }

        private uint[] ReadCode()
        {
            var code = new uint[readUint32()];
            for (var i = 0; i < code.Length; i++)
            {
                code[i] = readUint32();
            }

            return code;
        }

        private string ReadString()
        {
            var size = (uint) readByte() & 0xFF;
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