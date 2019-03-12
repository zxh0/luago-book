using System;
using LuaType = System.Int32;
using ArithOp = System.Int32;
using CompareOp = System.Int32;

namespace luavm.api
{
    public delegate int CsharpFunction(LuaState luaState);

    public partial interface LuaState
    {
        int LuaType { get; set; }
        int GetTop();
        int AbsIndex(int idx);
        bool CheckStack(int n);
        void Pop(int n);
        void Copy(int fromIdx, int toIdx);
        void PushValue(int idx);
        void Replace(int idx);
        void Insert(int idx);
        void Remove(int idx);
        void Rotate(int idx, int n);
        void SetTop(int idx);

        /* access functions (stack->c#) */
        string TypeName(LuaType tp);
        LuaType Type(int idx);
        bool IsNone(int idx);
        bool IsNil(int idx);
        bool IsNoneOrNil(int idx);
        bool IsBoolean(int idx);
        bool IsInteger(int idx);
        bool IsNumber(int idx);
        bool IsString(int idx);
        bool ToBoolean(int idx);
        long ToInteger(int idx);
        (long, bool) ToIntegerX(int idx);
        double ToNumber(int idx);
        (double, bool) ToNumberX(int idx);
        string ToString(int idx);
        (string, bool) ToStringX(int idx);

        /* push functions (c# -> stack) */
        void PushNil();
        void PushBoolean(bool b);
        void PushInteger(long n);
        void PushNumber(double n);
        void PushString(string s);

        void Arith(ArithOp op);
        bool Compare(int idx1, int idx2, CompareOp op);
        void Len(int idx);
        void Concat(int n);

        void NewTable();
        void CreateTable(int nArr, int nRec);
        LuaType GetTable(int idx);
        LuaType GetField(int idx, string k);
        LuaType GetI(int idx, long i);
        void SetTable(int idx);
        void SetField(int idx, string k);
        void SetI(int idx, long n);

        int Load(ref byte[] chunk, string chunkName, string mode);
        void Call(int nArgs, int nResults);

        void PushCsharpFunction(CsharpFunction f);
        bool IsCsharpFunction(int idx);
        CsharpFunction ToCsharpFunction(int idx);

        void PushGlobalTable();
        LuaType GetGlobal(string name);
        void SetGlobal(string name);
        void Register(string name, CsharpFunction f);

        void PushCsharpClosure(CsharpFunction f, int n);
    }
}