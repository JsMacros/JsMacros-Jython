package xyz.wagyourtail.jsmacrosjython.functions;

import java.util.List;

import org.python.core.PyFunction;

import org.python.core.PyObject;
import xyz.wagyourtail.jsmacros.extensionbase.Functions;
import xyz.wagyourtail.jsmacros.extensionbase.IFConsumer;
import xyz.wagyourtail.jsmacros.extensionbase.MethodWrapper;

public class FConsumerJython extends Functions implements IFConsumer<PyFunction, PyFunction, PyFunction> {
    
    
    public FConsumerJython(String libName) {
        super(libName);
    }

    public FConsumerJython(String libName, List<String> exclude) {
        super(libName, exclude);
    }
    
    
    @Override
    public MethodWrapper<Object, Object, Object> autoWrap(PyFunction c) {
        return new MethodWrapper<Object, Object, Object>() {
    
            @Override
            public Object get() {
                return runInt().__tojava__(Object.class);
            }
    
            @Override
            public boolean test(Object o) {
                return (boolean) runInt(o).__tojava__(boolean.class);
            }
    
            @Override
            public Object apply(Object o) {
                return runInt(o).__tojava__(Object.class);
            }
    
            @Override
            public boolean test(Object o, Object o2) {
                return (boolean) runInt(o, o2).__tojava__(boolean.class);
            }
    
            @Override
            public Object apply(Object o, Object o2) {
                return runInt(o, o2).__tojava__(Object.class);
            }
    
            @Override
            public int compare(Object o1, Object o2) {
                return (int) runInt(o1, o2).__tojava__(int.class);
            }
    
            @Override
            public void run() {
                runInt();
            }
    
            private PyObject runInt(Object...args) {
                return c._jcall(args);
            }
            
            @Override
            public void accept(Object arg0) {
                runInt(arg0);
                
            }

            @Override
            public void accept(Object arg0, Object arg1) {
                runInt(arg0, arg1);
            }
        };
    }
    
    @Override
    public MethodWrapper<Object, Object, Object> autoWrapAsync(PyFunction c) {
        return new MethodWrapper<Object, Object, Object>() {
    
            @Override
            public Object get() {
                return runInt().__tojava__(Object.class);
            }
    
            @Override
            public boolean test(Object o) {
                return (boolean) runInt(o).__tojava__(boolean.class);
            }
    
            @Override
            public Object apply(Object o) {
                return runInt(o).__tojava__(Object.class);
            }
    
            @Override
            public boolean test(Object o, Object o2) {
                return (boolean) runInt(o, o2).__tojava__(boolean.class);
            }
    
            @Override
            public Object apply(Object o, Object o2) {
                return runInt(o, o2).__tojava__(Object.class);
            }
    
            @Override
            public int compare(Object o1, Object o2) {
                return (int) runInt(o1, o2).__tojava__(int.class);
            }
            
            @Override
            public void run() {
                acceptInt();
            }
    
            private PyObject runInt(Object...args) {
                return c._jcall(args);
            }
    
            private void acceptInt(Object...args) {
                Thread t = new Thread(() -> {
                    c._jcall(args);
                });
                t.start();
            }
            
            @Override
            public void accept(Object arg0) {
                acceptInt(arg0);
                
            }

            @Override
            public void accept(Object arg0, Object arg1) {
                acceptInt(arg0, arg1);
            }
        };
    }
    

    @Override
    public MethodWrapper<Object, Object, Object> toConsumer(PyFunction c) {
        return autoWrap(c);
    }

    @Override
    public MethodWrapper<Object, Object, Object> toBiConsumer(PyFunction c) {
        return autoWrap(c);
    }

    @Override
    public MethodWrapper<Object, Object, Object> toAsyncConsumer(PyFunction c) {
        return autoWrapAsync(c);
    }

    @Override
    public MethodWrapper<Object, Object, Object> toAsyncBiConsumer(PyFunction c) {
        return autoWrapAsync(c);
    }
    
}
