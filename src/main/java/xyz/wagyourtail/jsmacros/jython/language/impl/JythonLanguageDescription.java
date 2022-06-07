package xyz.wagyourtail.jsmacros.jython.language.impl;

import org.python.core.*;
import org.python.util.PythonInterpreter;
import xyz.wagyourtail.jsmacros.core.Core;
import xyz.wagyourtail.jsmacros.core.config.ScriptTrigger;
import xyz.wagyourtail.jsmacros.core.event.BaseEvent;
import xyz.wagyourtail.jsmacros.core.language.BaseLanguage;
import xyz.wagyourtail.jsmacros.core.language.BaseWrappedException;
import xyz.wagyourtail.jsmacros.core.language.EventContainer;

import java.io.File;
import java.util.Map;

public class JythonLanguageDescription extends BaseLanguage<PythonInterpreter> {

    public JythonLanguageDescription(String extension, Core runner) {
        super(extension, runner);
    }

    public static PythonInterpreter createInterp(File folder) {
        PySystemState sys = new PySystemState();
        sys.path.append(Py.newString(folder.getAbsolutePath()));
        return new PythonInterpreter(null, sys);
    }

    protected void execContext(EventContainer<PythonInterpreter> ctx, Executor exec) throws Exception {
        try (PythonInterpreter interp = createInterp(ctx.getCtx().getContainedFolder())) {
            ctx.getCtx().setContext(interp);
            retrieveLibs(ctx.getCtx()).forEach(interp::set);
            
            exec.accept(interp);
        }
    }
    
    @Override
    protected void exec(EventContainer<PythonInterpreter> ctx, ScriptTrigger scriptTrigger, BaseEvent baseEvent) throws Exception {
        execContext(ctx, (interp) -> {
            interp.set("event", baseEvent);
            interp.set("file", ctx.getCtx().getFile());
            interp.set("context", ctx);

            interp.execfile(ctx.getCtx().getFile().getCanonicalPath());
        });
    }

    @Override
    protected void exec(EventContainer<PythonInterpreter> ctx, String script, BaseEvent event) throws Exception {
        execContext(ctx, (interp) -> {
            interp.set("event", event);
            interp.set("file", ctx.getCtx().getFile());
            interp.set("context", ctx);

            interp.exec(script);
        });
    }
    
    @Override
    public BaseWrappedException<?> wrapException(Throwable ex) {
        if (ex instanceof PyException) {
            String message = ((PyType) ((PyException) ex).type).fastGetName().replace("exceptions.", "");
            if (ex instanceof PySyntaxError) {
                if (((PyBaseExceptionDerived)((PySyntaxError) ex).value).args instanceof PyTuple) {
                    PyTuple exceptionData = (PyTuple) ((PyBaseExceptionDerived) ((PySyntaxError) ex).value).args;
                    message += ": " + exceptionData.get(0);
                    PyTuple locationData = (PyTuple) exceptionData.get(1);
                    String fileName = (String) locationData.get(0);
                    int line = (int) locationData.get(1);
                    int col = (int) locationData.get(2);
                    BaseWrappedException.SourceLocation loc = new BaseWrappedException.GuestLocation(new File(fileName), -1, -1, line, col);
                    return new BaseWrappedException<>(ex, message, loc, ((PyException) ex).traceback != null ? wrapTraceback(((PyException) ex).traceback) : null);
                }
            } else {
                if (((PyException) ex).value instanceof PyString) message += ": " + ((PyException) ex).value.asString();
                else if (ex.getCause() != null) {
                    String intMessage = ex.getCause().getMessage();
                    if (intMessage != null) message += ": " + intMessage;
                }
                return new BaseWrappedException<>(ex, message, null, ((PyException) ex).traceback != null ? wrapTraceback(((PyException) ex).traceback) : null);
            }
        }
        return null;
    }
    
    @Override
    public JythonScriptContext createContext(BaseEvent event, File file) {
        return new JythonScriptContext(event, file);
    }
    
    public BaseWrappedException<?> wrapTraceback(PyTraceback traceback) {
        BaseWrappedException.GuestLocation loc = new BaseWrappedException.GuestLocation(new File(traceback.tb_frame.f_code.co_filename), -1, -1, traceback.tb_lineno, -1);
        return new BaseWrappedException<>(traceback, " at " + traceback.tb_frame.f_code.co_name, loc, traceback.tb_next != null ? wrapTraceback((PyTraceback) traceback.tb_next) : null);
    }
    
    protected interface Executor {
        void accept(PythonInterpreter interpreter) throws Exception;
    }
}
