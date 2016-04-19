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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

class FixedTimeMap<K, V> {

    private static final Logger logger = LoggerFactory.getLogger(FixedTimeMap.class);
    private static final ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1, new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread();
            thread.setName("MemoryCounter-Cleanup");
            thread.setDaemon(true);
            thread.setPriority(Thread.MIN_PRIORITY);
            thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread t, Throwable e) {
                    logger.error(String.format("Clean-Up Job in MemoryCounter fails, can result in OutOfMemory", e));
                }
            });
            return thread;
        }
    });

    static {
        scheduledThreadPoolExecutor.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                cleanUp();
            }
        }, 5, 5, TimeUnit.MINUTES);
    }

    private final static Set<WeakReference<FixedTimeMap>> allMaps = new CopyOnWriteArraySet<>();
    private final ConcurrentHashMap<K, Entry<V>> map = new ConcurrentHashMap<>();

    public FixedTimeMap() {
        allMaps.add(new WeakReference<FixedTimeMap>(this));
    }

    private static void cleanUp() {
        for (WeakReference<FixedTimeMap> fixedTimeMapWeakReference : allMaps) {
            FixedTimeMap fixedTimeMap = fixedTimeMapWeakReference.get();
            if (fixedTimeMap != null) fixedTimeMap.cleanUpMap();
        }
    }

    private void cleanUpMap() {
        for (Iterator<Map.Entry<K, Entry<V>>> iterator = map.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<K, Entry<V>> entry = iterator.next();
            if (entry.getValue().isRemove()) {
                iterator.remove();
            }
        }
    }

    public V getOrCreate(K key, Function<? super K, ? extends V> mappingFunction, TimeUnit timeUnit, long duration) {
        return map
                .compute(key, (k, v) -> {
                    if (v == null || v.isRemove()) {
                        return new Entry<V>(mappingFunction.apply(k), timeUnit, duration);
                    } else {
                        v.refreshEndTime(timeUnit, duration);
                        return v;
                    }
                }).getValue();
    }

    public V get(K key) {
        Entry<V> entry = map.get(key);
        if (entry == null) {
            return null;
        }
        if (entry.isRemove()) {
            return null;
        }
        return entry.getValue();
    }

    static class Entry<T> {

        private final T value;
        private final AtomicLong endTime;

        private Entry(T value, TimeUnit timeUnit, long duration) {
            this.value = value;
            long l = System.currentTimeMillis() + timeUnit.toMillis(duration);
            this.endTime = new AtomicLong(l);
        }

        private boolean isRemove() {
            long currentTimeMillis = System.currentTimeMillis();
            if (logger.isDebugEnabled()) {
                logger.debug(String.format("isRemove %d > %d", currentTimeMillis, endTime.longValue()));
            }
            return currentTimeMillis > endTime.longValue();
        }

        private void refreshEndTime(TimeUnit timeUnit, long duration) {
            endTime.updateAndGet(current -> Math.max(current, System.currentTimeMillis() + timeUnit.toMillis(duration)));
        }

        T getValue() {
            return value;
        }

        public long getEndTime() {
            return endTime.longValue();
        }
    }

    public Set<Map.Entry<K, Entry<V>>> entrySet() {
        return map.entrySet();
    }
}
