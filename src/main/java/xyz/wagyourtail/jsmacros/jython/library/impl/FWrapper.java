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


        private void internal_accept(boolean await, Object... params) {

            // if in the same jython context and not async...
            if (await) {
                internal_apply(params);
                return;
            }

            Thread t = new Thread(() -> {
                ctx.bindThread(Thread.currentThread());
                try {
                    fn._jcall(params);
                } catch (Throwable ex) {
                    Core.getInstance().profile.logError(ex);
                } finally {
                    ctx.releaseBoundEventIfPresent(Thread.currentThread());
                    ctx.unbindThread(Thread.currentThread());

                    Core.getInstance().profile.joinedThreadStack.remove(Thread.currentThread());
                }
            });
            t.start();
        }

        private Object internal_apply(Object... params) {
            if (ctx.getBoundThreads().contains(Thread.currentThread())) {
                return fn._jcall(params).__tojava__(Object.class);
            }

            try {
                ctx.bindThread(Thread.currentThread());
                if (Core.getInstance().profile.checkJoinedThreadStack()) {
                    Core.getInstance().profile.joinedThreadStack.add(Thread.currentThread());
                }
                return fn._jcall(params).__tojava__(Object.class);
            } catch (Throwable ex) {
                throw new RuntimeException(ex);
            } finally {
                ctx.releaseBoundEventIfPresent(Thread.currentThread());
                ctx.unbindThread(Thread.currentThread());
                Core.getInstance().profile.joinedThreadStack.remove(Thread.currentThread());
            }
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
            return (R) internal_apply(t);
        }

        @Override
        public R apply(T t, U u) {
            return (R) internal_apply(t, u);
        }

        @Override
        public boolean test(T t) {
            return (boolean) internal_apply(t);
        }

        @Override
        public boolean test(T t, U u) {
            return (boolean) internal_apply(t, u);
        }

        @Override
        public void run() {
            internal_accept(await);
        }

        @Override
        public int compare(T o1, T o2) {
            return (int) internal_apply(o1, o2);
        }

        @Override
        public R get() {
            return (R) internal_apply();
        }

    }

}
