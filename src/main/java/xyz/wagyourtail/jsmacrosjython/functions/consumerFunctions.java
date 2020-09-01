package xyz.wagyourtail.jsmacrosjython.functions;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.python.core.PyFunction;

import xyz.wagyourtail.jsmacros.runscript.functions.Functions;

public class consumerFunctions extends Functions {
    
    
    public consumerFunctions(String libName) {
        super(libName);
    }

    public consumerFunctions(String libName, List<String> exclude) {
        super(libName, exclude);
    }
    
    public Consumer<Object> toConsumer(PyFunction c) {
        return (arg0) -> {
            c._jcall(new Object[] {arg0});
        };
    }
    
    public BiConsumer<Object, Object> toBiConsumer(PyFunction c) {
        return (arg0, arg1) -> {
            c._jcall(new Object[] {arg0, arg1});
        };
    }
    
    public Consumer<Object> toAsyncConsumer(PyFunction c) {
        return (arg0) -> {
            Thread t = new Thread(() -> {
                c._jcall(new Object[] {arg0});
            });
            t.start();
        };
    }
    
    public BiConsumer<Object, Object> toAsyncBiConsumer(PyFunction c) {
        return (arg0, arg1) -> {
            Thread t = new Thread(() -> {
                c._jcall(new Object[] {arg0, arg1});
            });
            t.start();
        };
    }
    
}
