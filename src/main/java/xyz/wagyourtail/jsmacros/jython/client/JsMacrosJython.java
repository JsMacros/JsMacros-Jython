package xyz.wagyourtail.jsmacros.jython.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.python.util.PythonInterpreter;
import xyz.wagyourtail.jsmacros.client.JsMacros;
import xyz.wagyourtail.jsmacros.jython.language.impl.JythonLanguageDescription;
import xyz.wagyourtail.jsmacros.jython.library.impl.FWrapper;

import java.io.File;

public class JsMacrosJython implements ModInitializer {
    public static boolean hasJEP = false;
    
    @Override
    public void onInitialize() {
        hasJEP = FabricLoader.getInstance().isModLoaded("jsmacros-jep");
        
        JsMacros.core.addLanguage(new JythonLanguageDescription(hasJEP ? "jython.py" : ".py", JsMacros.core));
        JsMacros.core.libraryRegistry.addLibrary(FWrapper.class);
        
        
        // pre-init
        Thread t = new Thread(() -> {
            try (PythonInterpreter interp = JythonLanguageDescription.createInterp(new File("./"))) {
                interp.exec("print(\"Jython Loaded.\")");
            } catch(Exception e) {
                e.printStackTrace();
            }
        });
        
        t.start();
    }

}
