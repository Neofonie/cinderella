package de.neofonie.common.cinderella.counter;

import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertEquals;

public class MemoryCounterTest {

    private final MemoryCounter memoryCounter = new MemoryCounter();

    @Test
    public void testDdos() throws Exception {
        final String KEY = "testDdos";
        assertEquals(false, memoryCounter.isBlacklisted(KEY));
        assertEquals(false, memoryCounter.isWhitelisted(KEY));
        assertEquals(false, memoryCounter.isDdos(KEY, 1, TimeUnit.SECONDS, 1));

        assertEquals(false, memoryCounter.isBlacklisted(KEY));
        assertEquals(false, memoryCounter.isWhitelisted(KEY));
        assertEquals(true, memoryCounter.isDdos(KEY, 1, TimeUnit.SECONDS, 1));
        doWait();

        assertEquals(false, memoryCounter.isBlacklisted(KEY));
        assertEquals(false, memoryCounter.isWhitelisted(KEY));
        assertEquals(false, memoryCounter.isDdos(KEY, 1, TimeUnit.SECONDS, 1));
    }

    @Test
    public void testBlacklist() throws Exception {
        final String KEY = "testBlacklist";

        assertEquals(false, memoryCounter.isBlacklisted(KEY));
        assertEquals(false, memoryCounter.isWhitelisted(KEY));
        memoryCounter.blacklist(KEY, TimeUnit.SECONDS, 1);

        assertEquals(true, memoryCounter.isBlacklisted(KEY));
        assertEquals(false, memoryCounter.isBlacklisted("foo"));
        assertEquals(false, memoryCounter.isWhitelisted(KEY));

        doWait();
        assertEquals(false, memoryCounter.isBlacklisted(KEY));
    }

    @Test
    public void testWhitelist() throws Exception {
        final String KEY = "testWhitelist";

        assertEquals(false, memoryCounter.isBlacklisted(KEY));
        assertEquals(false, memoryCounter.isWhitelisted(KEY));
        memoryCounter.whitelist(KEY, TimeUnit.SECONDS, 1);

        assertEquals(false, memoryCounter.isBlacklisted(KEY));
        assertEquals(true, memoryCounter.isWhitelisted(KEY));
        assertEquals(false, memoryCounter.isWhitelisted("foo"));

        doWait();
        assertEquals(false, memoryCounter.isWhitelisted(KEY));
    }

    private void doWait() {
        synchronized (this) {
            try {
                wait(1001L);
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
        }
    }
}