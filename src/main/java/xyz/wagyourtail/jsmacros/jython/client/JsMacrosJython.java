package xyz.wagyourtail.jsmacros.jython.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.python.util.PythonInterpreter;
import xyz.wagyourtail.jsmacros.client.JsMacros;
import xyz.wagyourtail.jsmacros.jython.language.impl.JythonLanguageDescription;
import xyz.wagyourtail.jsmacros.jython.library.impl.FConsumerJython;

public class JsMacrosJython implements ClientModInitializer {
    public static boolean hasJEP = false;
    
    @Override
    public void onInitializeClient() {
        hasJEP = FabricLoader.getInstance().isModLoaded("jsmacros-jep");
        JsMacros.core.addLanguage(new JythonLanguageDescription(hasJEP ? "jython.py" : ".py", JsMacros.core));
        JsMacros.core.sortLanguages();
        JsMacros.core.libraryRegistry.addLibrary(FConsumerJython.class);
        
        
        // pre-init
        Thread t = new Thread(() -> {
            try (PythonInterpreter interp = new PythonInterpreter()) {
                interp.exec("print(\"Jython Loaded.\")");
            } catch(Exception e) {
                e.printStackTrace();
            }
        });
        
        t.start();
    }

}
