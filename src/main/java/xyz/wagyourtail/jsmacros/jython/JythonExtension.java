package xyz.wagyourtail.jsmacros.jython;

import com.google.common.collect.Sets;
import org.python.core.*;
import org.python.util.PythonInterpreter;
import xyz.wagyourtail.jsmacros.core.Core;
import xyz.wagyourtail.jsmacros.core.extensions.Extension;
import xyz.wagyourtail.jsmacros.core.language.BaseLanguage;
import xyz.wagyourtail.jsmacros.core.language.BaseWrappedException;
import xyz.wagyourtail.jsmacros.core.library.BaseLibrary;
import xyz.wagyourtail.jsmacros.jython.language.impl.JythonLanguageDescription;
import xyz.wagyourtail.jsmacros.jython.library.impl.FWrapper;

import java.io.File;
import java.util.Set;

public class JythonExtension implements Extension {
    private static JythonLanguageDescription languageDescription;

    @Override
    public void init() {
        Thread t = new Thread(() -> {
            try (PythonInterpreter interp = JythonLanguageDescription.createInterp(new File("./"))) {
                interp.exec("print(\"Jython Loaded.\")");
            } catch(Exception e) {
                e.printStackTrace();
            }
        });
        t.start();
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public String getLanguageImplName() {
        return "jython";
    }

    @Override
    public ExtMatch extensionMatch(File file) {
        if (file.getName().endsWith(".py")) {
            if (file.getName().contains(getLanguageImplName())) {
                return ExtMatch.MATCH_WITH_NAME;
            } else {
                return ExtMatch.MATCH;
            }
        }
        return ExtMatch.NOT_MATCH;
    }

    @Override
    public String defaultFileExtension() {
        return "py";
    }

    @Override
    public BaseLanguage<?, ?> getLanguage(Core<?, ?> core) {
        if (languageDescription == null) {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(JythonExtension.class.getClassLoader());
            languageDescription = new JythonLanguageDescription(this, core);
            Thread.currentThread().setContextClassLoader(classLoader);
        }
        return languageDescription;
    }

    @Override
    public Set<Class<? extends BaseLibrary>> getLibraries() {
        return Sets.newHashSet(FWrapper.class);
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


    public BaseWrappedException<?> wrapTraceback(PyTraceback traceback) {
        BaseWrappedException.GuestLocation loc = new BaseWrappedException.GuestLocation(new File(traceback.tb_frame.f_code.co_filename), -1, -1, traceback.tb_lineno, -1);
        return new BaseWrappedException<>(traceback, " at " + traceback.tb_frame.f_code.co_name, loc, traceback.tb_next != null ? wrapTraceback((PyTraceback) traceback.tb_next) : null);
    }

    @Override
    public boolean isGuestObject(Object o) {
        return o instanceof PyObject;
    }

}
