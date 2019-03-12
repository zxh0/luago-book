using System;
using System.Linq;
using luavm.util;

namespace luavm.binchunk
{
    internal class Reader
    {
        public byte[] Data;

        public byte ReadByte()
        {
            var b = Data[0];
            Data = Data.Skip(1).ToArray();
            return b;
        }

        private uint ReadUint32()
        {
            var bytes = new byte[4];
            Array.ConstrainedCopy(Data, 0, bytes, 0, 4);
            Data = Data.Skip(4).ToArray();
            if (!BitConverter.IsLittleEndian)
            {
                Array.Reverse(bytes);
            }

            return BitConverter.ToUInt32(bytes, 0);
        }

        private long ReadLuaInteger()
        {
            return (long) ReadUint64();
        }

        private double ReadLuaNumber()
        {
            return BitConverter.Int64BitsToDouble((long) ReadUint64());
        }

        private ulong ReadUint64()
        {
            var bytes = new byte[8];
            Array.ConstrainedCopy(Data, 0, bytes, 0, 8);
            Data = Data.Skip(8).ToArray();
            if (!BitConverter.IsLittleEndian)
            {
                Array.Reverse(bytes);
            }

            return BitConverter.ToUInt64(bytes, 0);
        }

        private byte[] ReadBytes(uint n)
        {
            var bytes = new byte[n];
            Array.ConstrainedCopy(Data, 0, bytes, 0, (int) n);
            Data = Data.Skip((int) n).ToArray();
            return bytes;
        }

        public void CheckHeader()
        {
//            Console.WriteLine(Encoding.Default.GetString(readBytes(4)));
            if (ConvertUtil.Bytes2String(ReadBytes(4)) != BinaryChunk.LUA_SIGNATURE)
            {
                throw new Exception("not a precompiled chunk!");
            }

            if (ReadByte() != BinaryChunk.LUAC_VERSION)
            {
                throw new Exception("version mismatch!");
            }

            if (ReadByte() != BinaryChunk.LUAC_FORMAT)
            {
                throw new Exception("format mismatch!");
            }

            if (ConvertUtil.Bytes2String(ReadBytes(6)) != BinaryChunk.LUAC_DATA)
            {
                throw new Exception("corrupted!");
            }

            if (ReadByte() != BinaryChunk.CINT_SIZE)
            {
                throw new Exception("int size mismatch!");
            }

            var b = ReadByte();
            if (b != BinaryChunk.CSIZET_SIZE_32 && b != BinaryChunk.CSIZET_SIZE_64)
            {
                throw new Exception("size_t size mismatch!");
            }

            if (ReadByte() != BinaryChunk.INSTRUCTION_SIZE)
            {
                throw new Exception("instruction size mismatch!");
            }

            if (ReadByte() != BinaryChunk.LUA_INTEGER_SIZE)
            {
                throw new Exception("lua_Integer size mismatch!");
            }

            if (ReadByte() != BinaryChunk.LUA_NUMBER_SIZE)
            {
                throw new Exception("lua_Number size mismatch!");
            }

            if (ReadLuaInteger() != BinaryChunk.LUAC_INT)
            {
                throw new Exception("endianness mismatch!");
            }

            if (!ReadLuaNumber().Equals(BinaryChunk.LUAC_NUM))
            {
                throw new Exception("float format mismatch!");
            }
        }

        public Prototype ReadProto(string parentSource)
        {
            var source = ReadString();
            if (source == "")
            {
                source = parentSource;
            }

            return new Prototype
            {
                Source = source,
                LineDefined = ReadUint32(),
                LastLineDefined = ReadUint32(),
                NumParams = ReadByte(),
                IsVararg = ReadByte(),
                MaxStackSize = ReadByte(),
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
            var names = new string[ReadUint32()];
            for (var i = 0; i < names.Length; i++)
            {
                names[i] = ReadString();
            }

            return names;
        }

        private LocVar[] ReadLocVars()
        {
            var locVars = new LocVar[ReadUint32()];
            for (var i = 0; i < locVars.Length; i++)
            {
                locVars[i] = new LocVar
                {
                    VarName = ReadString(),
                    StartPc = ReadUint32(),
                    EndPc = ReadUint32()
                };
            }

            return locVars;
        }

        private uint[] ReadLineInfo()
        {
            var lineInfo = new uint[ReadUint32()];
            for (var i = 0; i < lineInfo.Length; i++)
            {
                lineInfo[i] = ReadUint32();
            }

            return lineInfo;
        }

        private Prototype[] ReadProtos(string parentSource)
        {
            var protos = new Prototype[ReadUint32()];
            for (var i = 0; i < protos.Length; i++)
            {
                protos[i] = new Prototype();
                protos[i] = ReadProto(parentSource);
            }

            return protos;
        }

        private Upvalue[] ReadUpvalues()
        {
            var upvalues = new Upvalue[ReadUint32()];
            for (var i = 0; i < upvalues.Length; i++)
            {
                upvalues[i] = new Upvalue
                {
                    Instack = ReadByte(),
                    Idx = ReadByte()
                };
            }

            return upvalues;
        }

        private object[] ReadConstants()
        {
            var constants = new object[ReadUint32()];
            for (var i = 0; i < constants.Length; i++)
            {
                constants[i] = ReadConstant();
            }

            return constants;
        }

        private object ReadConstant()
        {
            switch (ReadByte())
            {
                case BinaryChunk.TAG_NIL: return null;
                case BinaryChunk.TAG_BOOLEAN: return ReadByte() != 0;
                case BinaryChunk.TAG_INTEGER: return ReadLuaInteger();
                case BinaryChunk.TAG_NUMBER: return ReadLuaNumber();
                case BinaryChunk.TAG_SHORT_STR: return ReadString();
                case BinaryChunk.TAG_LONG_STR: return ReadString();
                default: throw new Exception("corrupted!");
            }
        }

        private uint[] ReadCode()
        {
            var code = new uint[ReadUint32()];
            for (var i = 0; i < code.Length; i++)
            {
                code[i] = ReadUint32();
            }

            return code;
        }

        private string ReadString()
        {
            var size = (uint) ReadByte() & 0xFF;
            if (size == 0)
            {
                return "";
            }

            if (size == 0xFF)
            {
                size = (uint) ReadUint64();
            }

            var bytes = ReadBytes(size - 1);
            return ConvertUtil.Bytes2String(bytes);
        }
    }
}