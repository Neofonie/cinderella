/*
 *
 * The MIT License (MIT)
 * Copyright (c) 2016 Neofonie GmbH
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package de.neofonie.cinderella.core.counter;

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
        assertEquals(memoryCounter.incrementAndGetNormalRequestCount(KEY, TimeUnit.SECONDS, 1L), 1L);

        assertEquals(false, memoryCounter.isBlacklisted(KEY));
        assertEquals(false, memoryCounter.isWhitelisted(KEY));
        assertEquals(memoryCounter.incrementAndGetNormalRequestCount(KEY, TimeUnit.SECONDS, 1L), 2L);
        doWait();

        assertEquals(false, memoryCounter.isBlacklisted(KEY));
        assertEquals(false, memoryCounter.isWhitelisted(KEY));
        assertEquals(memoryCounter.incrementAndGetNormalRequestCount(KEY, TimeUnit.SECONDS, 1L), 1L);
    }

    @Test
    public void testBlacklist() throws Exception {
        final String KEY = "testBlacklist";

        assertEquals(false, memoryCounter.isBlacklisted(KEY));
        assertEquals(false, memoryCounter.isWhitelisted(KEY));
        memoryCounter.blacklist(KEY, TimeUnit.SECONDS, 1L);

        assertEquals(true, memoryCounter.isBlacklisted(KEY));
        assertEquals(false, memoryCounter.isBlacklisted("foo"));
        assertEquals(false, memoryCounter.isWhitelisted(KEY));

        doWait();
        assertEquals(false, memoryCounter.isBlacklisted(KEY));
    }

    @Test
    public void testGetBlacklist() throws Exception {
        final String KEY = "testBlacklist";

        assertEquals(0, memoryCounter.getBlacklistedRequestCount(KEY));
        assertEquals(false, memoryCounter.isWhitelisted(KEY));
        memoryCounter.blacklist(KEY, TimeUnit.SECONDS, 1L);

        assertEquals(1, memoryCounter.getBlacklistedRequestCount(KEY));
        assertEquals(false, memoryCounter.isBlacklisted("foo"));
        assertEquals(true, memoryCounter.isBlacklisted(KEY));
        assertEquals(false, memoryCounter.isWhitelisted(KEY));

        doWait();
        assertEquals(false, memoryCounter.isBlacklisted(KEY));
        assertEquals(0, memoryCounter.getBlacklistedRequestCount(KEY));
    }

    @Test
    public void testWhitelist() throws Exception {
        final String KEY = "testWhitelist";

        assertEquals(false, memoryCounter.isBlacklisted(KEY));
        assertEquals(false, memoryCounter.isWhitelisted(KEY));
        memoryCounter.whitelist(KEY, TimeUnit.SECONDS, 1L);

        assertEquals(false, memoryCounter.isBlacklisted(KEY));
        assertEquals(true, memoryCounter.isWhitelisted(KEY));
        assertEquals(false, memoryCounter.isWhitelisted("foo"));

        doWait();
        assertEquals(false, memoryCounter.isWhitelisted(KEY));
    }

    @Test
    public void testResetCounter() throws Exception {
        final String KEY = "testResetCounter";
        assertEquals(memoryCounter.incrementAndGetNormalRequestCount(KEY, TimeUnit.MINUTES, 1L), 1L);
        assertEquals(memoryCounter.incrementAndGetNormalRequestCount(KEY, TimeUnit.MINUTES, 1L), 2L);
        assertEquals(memoryCounter.incrementAndGetNormalRequestCount(KEY, TimeUnit.MINUTES, 1L), 3L);
        memoryCounter.resetCounter(KEY);
        assertEquals(memoryCounter.incrementAndGetNormalRequestCount(KEY, TimeUnit.MINUTES, 1L), 1L);

    }

    @Test
    public void testResetBlacklistCount() throws Exception {
        final String KEY = "testResetBlacklistCount";

        assertEquals(false, memoryCounter.isBlacklisted(KEY));

        memoryCounter.blacklist(KEY, TimeUnit.MINUTES, 1L);
        assertEquals(true, memoryCounter.isBlacklisted(KEY));

        memoryCounter.resetBlacklistCount(KEY);
        assertEquals(false, memoryCounter.isBlacklisted(KEY));
    }

    private void doWait() {
        synchronized (this) {
            try {
                wait(2000L);
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
        }
    }
}