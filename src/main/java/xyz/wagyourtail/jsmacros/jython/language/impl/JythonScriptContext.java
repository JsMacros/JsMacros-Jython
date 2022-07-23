package xyz.wagyourtail.jsmacros.jython.language.impl;

import org.python.util.PythonInterpreter;
import xyz.wagyourtail.jsmacros.core.event.BaseEvent;
import xyz.wagyourtail.jsmacros.core.language.BaseScriptContext;

import java.io.File;

public class JythonScriptContext extends BaseScriptContext<PythonInterpreter> {
    public JythonScriptContext(BaseEvent event, File file) {
        super(event, file);
    }
    
    @Override
    public void closeContext() {
    super.closeContext();
    PythonInterpreter ctx = context;
        if (ctx != null) {
            ctx.close();
        }
    }

    @Override
    public boolean isMultiThreaded() {
        return true;
    }

}
