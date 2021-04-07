package xyz.wagyourtail.jsmacros.jython.language.impl;

import org.python.util.PythonInterpreter;
import xyz.wagyourtail.jsmacros.core.language.ScriptContext;

public class JythonScriptContext extends ScriptContext<PythonInterpreter> {
    boolean closed = false;
    
    @Override
    public boolean isContextClosed() {
        return super.isContextClosed() || closed;
    }
    
    @Override
    public void closeContext() {
        if (context != null) {
            PythonInterpreter ctx = context.get();
            if (ctx != null) {
                ctx.close();
                closed = true;
            }
        }
    }
    
}
