package xyz.wagyourtail.jsmacros.jython.language.impl;

import org.python.core.*;
import org.python.util.PythonInterpreter;
import xyz.wagyourtail.jsmacros.core.Core;
import xyz.wagyourtail.jsmacros.core.config.ScriptTrigger;
import xyz.wagyourtail.jsmacros.core.event.BaseEvent;
import xyz.wagyourtail.jsmacros.core.language.BaseLanguage;
import xyz.wagyourtail.jsmacros.core.language.BaseWrappedException;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.Executor;

public class JythonLanguageDescription extends BaseLanguage {

    public JythonLanguageDescription(String extension, Core runner) {
        super(extension, runner);
    }
    
    protected void execContext(Executor exec) throws Exception {
        try (PythonInterpreter interp = new PythonInterpreter()) {
            retrieveLibs(interp).forEach(interp::set);
            
            exec.accept(interp);
        }
    }
    
    @Override
    public void exec(ScriptTrigger scriptTrigger, File file, BaseEvent baseEvent) throws Exception {
        execContext((interp) -> {
            interp.set("event", baseEvent);
            interp.set("file", file);
        
            interp.exec("import os\nos.chdir('"
                + file.getParentFile().getCanonicalPath().replaceAll("\\\\", "/") + "')");
            interp.execfile(file.getCanonicalPath());
        });
    }
    
    @Override
    public void exec(String script, Map<String, Object> globals, Path path) throws Exception {
        execContext((interp) -> {
            globals.forEach(interp::set);
            
            interp.exec(script);
        });
    }
    
    @Override
    public BaseWrappedException<?> wrapException(Throwable ex) {
        if (ex instanceof PyException) {
            String message = ((PyType) ((PyException) ex).type).fastGetName().replace("exceptions.", "");
            if (ex instanceof PySyntaxError) {
                PyTuple exceptionData = (PyTuple) ((PySyntaxError) ex).value;
                message += ": " + exceptionData.get(0);
                PyTuple locationData = (PyTuple) exceptionData.get(1);
                String fileName = (String) locationData.get(0);
                int line = (int) locationData.get(1);
                int col = (int) locationData.get(2);
                BaseWrappedException.SourceLocation loc = new BaseWrappedException.GuestLocation(new File(fileName), -1, -1, line, col);
                return new BaseWrappedException<>(ex, message, loc, ((PyException) ex).traceback != null ? wrapTraceback(((PyException) ex).traceback) : null);
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
    
    public BaseWrappedException<?> wrapTraceback(PyTraceback traceback) {
        BaseWrappedException.GuestLocation loc = new BaseWrappedException.GuestLocation(new File(traceback.tb_frame.f_code.co_filename), -1, -1, traceback.tb_lineno, -1);
        return new BaseWrappedException<>(traceback, " at " + traceback.tb_frame.f_code.co_name, loc, traceback.tb_next != null ? wrapTraceback((PyTraceback) traceback.tb_next) : null);
    }
    
    protected interface Executor {
        void accept(PythonInterpreter interpreter) throws Exception;
    }
}
