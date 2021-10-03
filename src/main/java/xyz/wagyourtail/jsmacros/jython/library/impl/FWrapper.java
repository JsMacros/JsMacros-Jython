package xyz.wagyourtail.jsmacros.jython.library.impl;

import org.python.core.PyFunction;
import org.python.util.PythonInterpreter;
import xyz.wagyourtail.jsmacros.core.Core;
import xyz.wagyourtail.jsmacros.core.MethodWrapper;
import xyz.wagyourtail.jsmacros.core.language.BaseLanguage;
import xyz.wagyourtail.jsmacros.core.language.BaseScriptContext;
import xyz.wagyourtail.jsmacros.core.library.IFWrapper;
import xyz.wagyourtail.jsmacros.core.library.Library;
import xyz.wagyourtail.jsmacros.core.library.PerExecLanguageLibrary;
import xyz.wagyourtail.jsmacros.jython.language.impl.JythonLanguageDescription;

import java.util.concurrent.Semaphore;

@Library(value = "JavaWrapper", languages = JythonLanguageDescription.class)
public class FWrapper extends PerExecLanguageLibrary<PythonInterpreter> implements IFWrapper<PyFunction> {
    
    public FWrapper(BaseScriptContext<PythonInterpreter> ctx, Class<BaseLanguage<PythonInterpreter>> language) {
        super(ctx, language);
    }
    
    @Override
    public <A, B, R> MethodWrapper<A, B, R, ?> methodToJava(PyFunction pyFunction) {
        return new JythonMethodWrapper<>(pyFunction, true, ctx);
    }
    
    @Override
    public <A, B, R> MethodWrapper<A, B, R, ?> methodToJavaAsync(PyFunction pyFunction) {
        return new JythonMethodWrapper<>(pyFunction, false, ctx);
    }
    
    @Override
    public void stop() {
        ctx.closeContext();
    }

    private static class JythonMethodWrapper<T, U, R> extends MethodWrapper<T, U, R, BaseScriptContext<PythonInterpreter>> {
        private final PyFunction fn;
        private final boolean await;

        private JythonMethodWrapper(PyFunction fn, boolean await, BaseScriptContext<PythonInterpreter> ctx) {
            super(ctx);
            this.fn = fn;
            this.await = await;
        }


        private Object internal_accept(boolean await, Object... params) {

            // if in the same lua context and not async...
            if (await) {
                if (ctx.getBoundThreads().contains(Thread.currentThread())) {
                    return fn._jcall(params).__tojava__(Object.class);
                }

                ctx.bindThread(Thread.currentThread());
            }

            Object[] retval = {null};
            Throwable[] error = {null};
            Semaphore lock = new Semaphore(0);
            boolean joinedThread = Core.instance.profile.checkJoinedThreadStack();


            Thread t = new Thread(() -> {
                ctx.bindThread(Thread.currentThread());
                try {
                    if (await && joinedThread) {
                        Core.instance.profile.joinedThreadStack.add(Thread.currentThread());
                    }
                    retval[0] = fn._jcall(params).__tojava__(Object.class);
                } catch (Throwable ex) {
                    if (!await) {
                        Core.instance.profile.logError(ex);
                    }
                    error[0] = ex;
                } finally {
                    ctx.unbindThread(Thread.currentThread());
                    Core.instance.profile.joinedThreadStack.remove(Thread.currentThread());

                    ctx.releaseBoundEventIfPresent(Thread.currentThread());

                    lock.release();
                }
            });
            t.start();

            if (await) {
                try {
                    lock.acquire();
                    if (error[0] != null) throw new RuntimeException(error[0]);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                } finally {
                    ctx.unbindThread(Thread.currentThread());
                }
            }
            return retval[0];
        }

        @Override
        public void accept(T t) {
            internal_accept(await, t);
        }

        @Override
        public void accept(T t, U u) {
            internal_accept(await, t, u);
        }

        @Override
        public R apply(T t) {
            return (R) internal_accept(true, t);
        }

        @Override
        public R apply(T t, U u) {
            return (R) internal_accept(true, t, u);
        }

        @Override
        public boolean test(T t) {
            return (boolean) internal_accept(true, t);
        }

        @Override
        public boolean test(T t, U u) {
            return (boolean) internal_accept(true, t, u);
        }

        @Override
        public void run() {
            internal_accept(await);
        }

        @Override
        public int compare(T o1, T o2) {
            return (int) internal_accept(true, o1, o2);
        }

        @Override
        public R get() {
            return (R) internal_accept(true);
        }

    }

}
