package xyz.wagyourtail.jsmacros.core;

import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;
import xyz.wagyourtail.jsmacros.core.event.impl.EventCustom;
import xyz.wagyourtail.jsmacros.core.language.EventContainer;
import xyz.wagyourtail.jsmacros.stubs.CoreInstanceCreator;
import xyz.wagyourtail.jsmacros.stubs.EventRegistryStub;
import xyz.wagyourtail.jsmacros.stubs.ProfileStub;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CoreTest {
    @Language("py")
    private final String TEST_SCRIPT = """
        event.putString("rp1", "Hello World!")
        JavaWrapper.methodToJava(lambda: event.putString("rp2", "Hello World!")).run()
        JavaWrapper.methodToJavaAsync(lambda: event.putString("rp3", "Hello World!")).run()
        """;
    
    @Test
    public void test() throws InterruptedException {
        Core<ProfileStub, EventRegistryStub> core = CoreInstanceCreator.createCore();
        EventCustom event = new EventCustom("test");
        EventContainer<?> ev = core.exec("py", TEST_SCRIPT, null, event, null, null);
        ev.awaitLock(() -> {});
        Thread.sleep(100);
        assertEquals("{rp1=Hello World!, rp3=Hello World!, rp2=Hello World!}", event.getUnderlyingMap().toString());
    }

}
