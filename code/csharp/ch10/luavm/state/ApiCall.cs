using System;
using System.Linq;
using luavm.api;

namespace luavm.state
{
    public partial struct LuaState
    {
        public int Load(ref byte[] chunk, string chunkName, string mode)
        {
            var proto = binchunk.BinaryChunk.Undump(chunk);
            var c = Closure.newLuaClosure(proto);
            stack.push(c);
            if (proto.Upvalues.Length > 0)
            {
                var env = registry.get(Consts.LUA_RIDX_GLOBALS);
                c.upvals[0] = new Upvalue
                {
                    val = env
                };
            }

            return 0;
        }

        public void Call(int nArgs, int nResults)
        {
            var val = stack.get(-(nArgs + 1));
            if (val != null && val.GetType().IsEquivalentTo(typeof(Closure)))
            {
                var c = (Closure) val;
                if (c.proto != null)
                {
                    callLuaClosure(nArgs, nResults, c);
                }
                else
                {
                    callCsharpClosure(nArgs, nResults, c);
                }
            }
            else
            {
                throw new Exception("not function!");
            }
        }

        private void callCsharpClosure(int nArgs, int nResults, Closure c)
        {
            // create new lua stack
            var newStack = LuaStack.newLuaStack(nArgs + Consts.LUA_MINSTACK, this);
            newStack.closure = c;

            // pass args, pop func
            var args = stack.popN(nArgs);
            newStack.pushN(args, nArgs);
            stack.pop();

            // run closure
            pushLuaStack(newStack);
            var r = c.csharpFunc(this);
            popLuaStack();

            // return results
            if (nResults != 0)
            {
                var results = newStack.popN(r);
                stack.check(results.Length);
                stack.pushN(results, nResults);
            }
        }

        void callLuaClosure(int nArgs, int nResults, Closure c)
        {
            var nRegs = (int) c.proto.MaxStackSize;
            var nParams = (int) c.proto.NumParams;
            var isVararg = c.proto.IsVararg == 1;

            // create new lua stack
            var newStack = LuaStack.newLuaStack(nRegs + Consts.LUA_MINSTACK, this);
            newStack.closure = c;

            // pass args, pop func
            var funcAndArgs = stack.popN(nArgs + 1);
            newStack.pushN(funcAndArgs.Skip(1).ToArray(), nParams);
            newStack.top = nRegs;
            if (nArgs > nParams && isVararg)
            {
                newStack.varargs = funcAndArgs.Skip(nParams + 1).ToArray();
            }

            // run closure
            pushLuaStack(newStack);
            runLuaClosure();
            popLuaStack();

            // return results
            if (nResults != 0)
            {
                var results = newStack.popN(newStack.top - nRegs);
                stack.check(results.Length);
                stack.pushN(results, nResults);
            }
        }

        void runLuaClosure()
        {
            for (;;)
            {
                var inst = new vm.Instruction(Fetch());
                inst.Execute(ref this);
                if (inst.Opcode() == vm.OpCodes.OP_RETURN)
                {
                    break;
                }
            }
        }
    }
}