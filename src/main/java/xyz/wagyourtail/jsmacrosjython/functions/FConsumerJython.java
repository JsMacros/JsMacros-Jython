package xyz.wagyourtail.jsmacrosjython.functions;

import java.util.List;

import org.python.core.PyFunction;

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
    public MethodWrapper<Object, Object> autoWrap(PyFunction c) {
        return new MethodWrapper<Object, Object>() {

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
    public MethodWrapper<Object, Object> autoWrapAsync(PyFunction c) {
        return new MethodWrapper<Object, Object>() {

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
    public MethodWrapper<Object, Object> toConsumer(PyFunction c) {
        return autoWrap(c);
    }

    @Override
    public MethodWrapper<Object, Object> toBiConsumer(PyFunction c) {
        return autoWrap(c);
    }

    @Override
    public MethodWrapper<Object, Object> toAsyncConsumer(PyFunction c) {
        return autoWrapAsync(c);
    }

    @Override
    public MethodWrapper<Object, Object> toAsyncBiConsumer(PyFunction c) {
        return autoWrapAsync(c);
    }
    
}
