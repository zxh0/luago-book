using System;
using System.Linq;
using luavm.api;
using luavm.binchunk;
using luavm.vm;

namespace luavm.state
{
    public partial class LuaState
    {
        public int Load(ref byte[] chunk, string chunkName, string mode)
        {
            var proto = BinaryChunk.Undump(chunk);
            var c = Closure.newLuaClosure(proto);
            stack.push(c);
            if (proto.Upvalues.Length > 0)
            {
                var env = registry.get(Consts.LUA_RIDX_GLOBALS);
                c.upvals[0] = new Upvalue(env);
            }

            return 0;
        }

        public void Call(int nArgs, int nResults)
        {
            var val = stack.get(-(nArgs + 1));
            var f = val is Closure ? val : null;
            if (f is null)
            {
                var mf = getMetafield(val, "__call", this);
                if (mf is Closure)
                {
                    stack.push(null);
                    Insert(-(nArgs + 2));
                    nArgs += 1;
                    f = mf;
                }
            }

            if (f != null)
            {
                var closure = (Closure) f;
                if (closure.proto != null)
                {
                    callLuaClosure(nArgs, nResults, closure);
                }
                else
                {
                    callCsharpClosure(nArgs, nResults, closure);
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
            var newStack = new LuaStack {state = this, closure = c};

            // pass args, pop func
            if (nArgs > 0)
            {
                newStack.pushN(stack.popN(nArgs), nArgs);
            }

            stack.pop();

            // run closure
            pushLuaStack(newStack);
            var r = c.csharpFunc(this);
            popLuaStack();

            // return results
            if (nResults != 0)
            {
                var results = newStack.popN(r);
                //stack.check(results.Length);
                stack.pushN(results, nResults);
            }
        }

        void callLuaClosure(int nArgs, int nResults, Closure c)
        {
            var nRegs = c.proto.MaxStackSize;
            var nParams = c.proto.NumParams;
            var isVararg = c.proto.IsVararg == 1;

            // create new lua stack
            var newStack = new LuaStack {closure = c};

            // pass args, pop func
            var funcAndArgs = stack.popN(nArgs + 1);
            newStack.pushN(funcAndArgs.Skip(1).ToArray().ToList(), nParams);
            if (nArgs > nParams && isVararg)
            {
                newStack.varargs = funcAndArgs.Skip(nParams + 1).ToArray().ToList();
            }

            // run closure
            pushLuaStack(newStack);
            SetTop(nRegs);
            runLuaClosure();
            popLuaStack();

            // return results
            if (nResults != 0)
            {
                var results = newStack.popN(newStack.top() - nRegs);
                //stack.check(results.size())
                stack.pushN(results, nResults);
            }
        }

        void runLuaClosure()
        {
            for (;;)
            {
                var i = Fetch();
                var inst = new Instruction(i);
                var opcode = inst.Opcode();
                inst.Execute(this);
                if (opcode == OpCodes.OP_RETURN)
                {
                    break;
                }
            }
        }
    }
}