package xyz.wagyourtail.jsmacros.jython.library.impl;

import org.python.core.PyFunction;
import org.python.core.PyObject;
import xyz.wagyourtail.jsmacros.client.JsMacros;
import xyz.wagyourtail.jsmacros.core.Core;
import xyz.wagyourtail.jsmacros.core.MethodWrapper;
import xyz.wagyourtail.jsmacros.core.language.BaseLanguage;
import xyz.wagyourtail.jsmacros.core.language.ContextContainer;
import xyz.wagyourtail.jsmacros.core.library.IFWrapper;
import xyz.wagyourtail.jsmacros.core.library.Library;
import xyz.wagyourtail.jsmacros.core.library.PerLanguageLibrary;
import xyz.wagyourtail.jsmacros.jython.language.impl.JythonLanguageDescription;
import xyz.wagyourtail.jsmacros.jython.language.impl.JythonScriptContext;

import java.util.concurrent.Semaphore;

@Library(value = "JavaWrapper", languages = JythonLanguageDescription.class)
public class FConsumerJython extends PerLanguageLibrary implements IFWrapper<PyFunction> {
    
    public FConsumerJython(Class<? extends BaseLanguage<?>> language) {
        super(language);
    }
    
    @Override
    public <A, B, R> MethodWrapper<A, B, R> methodToJava(PyFunction pyFunction) {
        JythonScriptContext currentContext = (JythonScriptContext) JsMacros.core.threadContext.get(Thread.currentThread());
        return new MethodWrapper<A, B, R>() {
            @Override
            public void accept(A a) {
                runInt(a);
            }
    
            @Override
            public void accept(A a, B b) {
                runInt(a, b);
            }
    
            @Override
            public R apply(A a) {
                return (R) runInt(a).__tojava__(Object.class);
            }
    
            @Override
            public R apply(A a, B b) {
                return (R) runInt(a, b).__tojava__(Object.class);
            }
    
            @Override
            public boolean test(A a) {
                return (boolean) runInt(a).__tojava__(boolean.class);
            }
    
            @Override
            public boolean test(A a, B b) {
                return (boolean) runInt(a, b).__tojava__(boolean.class);
            }
    
            @Override
            public void run() {
                runInt();
            }
    
            @Override
            public int compare(A o1, A o2) {
                return (int) runInt(o1, o2).__tojava__(int.class);
            }
    
            @Override
            public R get() {
                return (R) runInt().__tojava__(Object.class);
            }
    
            private PyObject runInt(Object...args) {
                Core.instance.threadContext.put(Thread.currentThread(), currentContext);
                try {
                    return pyFunction._jcall(args);
                } finally {
                    Core.instance.threadContext.remove(Thread.currentThread());
                }
            }
        };
    }
    
    @Override
    public <A, B, R> MethodWrapper<A, B, R> methodToJavaAsync(PyFunction pyFunction) {
        JythonScriptContext currentContext = (JythonScriptContext) JsMacros.core.threadContext.get(Thread.currentThread());
        return new MethodWrapper<A, B, R>() {
            @Override
            public void accept(A a) {
                acceptInt(a);
            }
    
            @Override
            public void accept(A a, B b) {
                acceptInt(a, b);
            }
    
            @Override
            public R apply(A a) {
                return (R) runInt(a).__tojava__(Object.class);
            }
    
            @Override
            public R apply(A a, B b) {
                return (R) runInt(a, b).__tojava__(Object.class);
            }
    
            @Override
            public boolean test(A a) {
                return (boolean) runInt(a).__tojava__(boolean.class);
            }
    
            @Override
            public boolean test(A a, B b) {
                return (boolean) runInt(a, b).__tojava__(boolean.class);
            }
    
            @Override
            public void run() {
                acceptInt();
            }
    
            @Override
            public int compare(A o1, A o2) {
                return (int) runInt(o1, o2).__tojava__(int.class);
            }
    
            @Override
            public R get() {
                return (R) runInt().__tojava__(Object.class);
            }
    
            private PyObject runInt(Object...args) {
                Core.instance.threadContext.put(Thread.currentThread(), currentContext);
                try {
                    return pyFunction._jcall(args);
                } finally {
                    Core.instance.threadContext.remove(Thread.currentThread());
                }
            }
    
            private void acceptInt(Object...args) {
                Thread t = new Thread(() -> {
                    Core.instance.threadContext.put(Thread.currentThread(), currentContext);
                    pyFunction._jcall(args);
                });
                t.start();
            }
        };
    }
    
    @Override
    public void stop() {
        Core.instance.threadContext.get(Thread.currentThread()).closeContext();
    }

    private static class JythonMethodWrapper<T, U, R> extends MethodWrapper<T, U, R> {
        private final PyFunction fn;
        private final boolean await;
        private final JythonScriptContext ctx;

        private JythonMethodWrapper(PyFunction fn, boolean await, JythonScriptContext ctx) {
            this.fn = fn;
            this.await = await;
            this.ctx = ctx;
        }


        private Object internal_accept(boolean await, Object... params) {

            // if in the same lua context and not async...
            if (Core.instance.threadContext.get(Thread.currentThread()) == ctx && await) {
                return fn._jcall(params).__tojava__(Object.class);
            }

            Object[] retval = {null};
            Throwable[] error = {null};
            Semaphore lock = new Semaphore(0);

            Thread t = new Thread(() -> {
                Core.instance.threadContext.put(Thread.currentThread(), ctx);
                try {
                    retval[0] = fn._jcall(params).__tojava__(Object.class);
                } catch (Throwable ex) {
                    if (!await) {
                        Core.instance.profile.logError(ex);
                    }
                    error[0] = ex;
                } finally {
                    ContextContainer<?> cc = Core.instance.eventContexts.get(Thread.currentThread());
                    if (cc != null) cc.releaseLock();

                    lock.release();
                }
            });
            t.start();

            if (await) {
                try {
                    lock.acquire();
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
                if (error[0] != null) throw new RuntimeException(error[0]);
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
