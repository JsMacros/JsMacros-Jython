package xyz.wagyourtail.jsmacros.jython.library.impl;

import org.python.core.PyFunction;
import org.python.core.PyObject;
import xyz.wagyourtail.jsmacros.client.JsMacros;
import xyz.wagyourtail.jsmacros.core.Core;
import xyz.wagyourtail.jsmacros.core.MethodWrapper;
import xyz.wagyourtail.jsmacros.core.language.BaseLanguage;
import xyz.wagyourtail.jsmacros.core.library.IFWrapper;
import xyz.wagyourtail.jsmacros.core.library.Library;
import xyz.wagyourtail.jsmacros.core.library.PerLanguageLibrary;
import xyz.wagyourtail.jsmacros.jython.language.impl.JythonLanguageDescription;
import xyz.wagyourtail.jsmacros.jython.language.impl.JythonScriptContext;

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
    
}
