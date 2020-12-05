package xyz.wagyourtail.jsmacros.jython.library.impl;

import org.python.core.PyFunction;
import org.python.core.PyObject;
import xyz.wagyourtail.jsmacros.core.MethodWrapper;
import xyz.wagyourtail.jsmacros.core.language.BaseLanguage;
import xyz.wagyourtail.jsmacros.core.library.IFConsumer;
import xyz.wagyourtail.jsmacros.core.library.Library;
import xyz.wagyourtail.jsmacros.core.library.PerExecLanguageLibrary;
import xyz.wagyourtail.jsmacros.jython.language.impl.JythonLanguageDescription;

@Library(value = "consumer", languages = JythonLanguageDescription.class)
public class FConsumerJython extends PerExecLanguageLibrary<IFConsumer> implements IFConsumer<PyFunction, PyFunction, PyFunction> {
    
    public FConsumerJython(Class<? extends BaseLanguage> language, Object context, Thread thread) {
        super(language, context, thread);
    }
    
    @Override
    public <A, B, R> MethodWrapper<A, B, R> toConsumer(PyFunction pyFunction) {
        return autoWrap(pyFunction);
    }
    
    @Override
    public <A, B, R> MethodWrapper<A, B, R> toBiConsumer(PyFunction pyFunction) {
        return autoWrap(pyFunction);
    }
    
    @Override
    public <A, B, R> MethodWrapper<A, B, R> toAsyncConsumer(PyFunction pyFunction) {
        return autoWrapAsync(pyFunction);
    }
    
    @Override
    public <A, B, R> MethodWrapper<A, B, R> toAsyncBiConsumer(PyFunction pyFunction) {
        return autoWrapAsync(pyFunction);
    }
    
    @Override
    public <A, B, R> MethodWrapper<A, B, R> autoWrap(PyFunction pyFunction) {
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
                return pyFunction._jcall(args);
            }
        };
    }
    
    @Override
    public <A, B, R> MethodWrapper<A, B, R> autoWrapAsync(PyFunction pyFunction) {
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
                return pyFunction._jcall(args);
            }
    
            private void acceptInt(Object...args) {
                Thread t = new Thread(() -> pyFunction._jcall(args));
                t.start();
            }
        };
    }
    
}
