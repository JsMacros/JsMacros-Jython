package xyz.wagyourtail.jsmacros.jython.language.impl;

import org.python.core.*;
import org.python.util.PythonInterpreter;
import xyz.wagyourtail.jsmacros.core.Core;
import xyz.wagyourtail.jsmacros.core.config.ScriptTrigger;
import xyz.wagyourtail.jsmacros.core.event.BaseEvent;
import xyz.wagyourtail.jsmacros.core.extensions.Extension;
import xyz.wagyourtail.jsmacros.core.language.BaseLanguage;
import xyz.wagyourtail.jsmacros.core.language.EventContainer;

import java.io.File;

public class JythonLanguageDescription extends BaseLanguage<PythonInterpreter, JythonScriptContext> {

    public JythonLanguageDescription(Extension extension, Core runner) {
        super(extension, runner);
    }

    public static PythonInterpreter createInterp(File folder) {
        PySystemState sys = new PySystemState();
        sys.path.append(Py.newStringOrUnicode(folder.getAbsolutePath()));
        return new PythonInterpreter(null, sys);
    }

    protected void execContext(EventContainer<JythonScriptContext> ctx, Executor exec) throws Exception {
        try (PythonInterpreter interp = createInterp(ctx.getCtx().getContainedFolder())) {
            ctx.getCtx().setContext(interp);
            retrieveLibs(ctx.getCtx()).forEach(interp::set);
            
            exec.accept(interp);
        }
    }
    
    @Override
    protected void exec(EventContainer<JythonScriptContext> ctx, ScriptTrigger scriptTrigger, BaseEvent baseEvent) throws Exception {
        execContext(ctx, (interp) -> {
            interp.set("event", baseEvent);
            interp.set("file", ctx.getCtx().getFile());
            interp.set("context", ctx);

            interp.execfile(ctx.getCtx().getFile().getCanonicalPath());
        });
    }

    @Override
    protected void exec(EventContainer<JythonScriptContext> ctx, String lang, String script, BaseEvent event) throws Exception {
        execContext(ctx, (interp) -> {
            interp.set("event", event);
            interp.set("file", ctx.getCtx().getFile());
            interp.set("context", ctx);

            interp.exec(script);
        });
    }
    
    @Override
    public JythonScriptContext createContext(BaseEvent event, File file) {
        return new JythonScriptContext(event, file);
    }
    
    protected interface Executor {
        void accept(PythonInterpreter interpreter) throws Exception;
    }
}
