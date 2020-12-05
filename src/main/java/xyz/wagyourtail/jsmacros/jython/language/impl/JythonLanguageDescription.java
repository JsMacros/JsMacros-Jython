package xyz.wagyourtail.jsmacros.jython.language.impl;

import org.python.util.PythonInterpreter;
import xyz.wagyourtail.jsmacros.core.Core;
import xyz.wagyourtail.jsmacros.core.config.ScriptTrigger;
import xyz.wagyourtail.jsmacros.core.event.BaseEvent;
import xyz.wagyourtail.jsmacros.core.language.BaseLanguage;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;

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
    
    protected interface Executor {
        void accept(PythonInterpreter interpreter) throws Exception;
    }
}
