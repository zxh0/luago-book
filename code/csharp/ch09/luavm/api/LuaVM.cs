using luavm.binchunk;

namespace luavm.api
{
    public partial interface LuaState
    {
        int PC();
        void AddPC(int n);
        uint Fetch();
        void GetConst(int idx);
        void GetRK(int rk);
        int RegisterCount();
        void LoadVararg(int n);
        void LoadProto(int idx);
    }
}