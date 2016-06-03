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

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class MemoryCounter implements Counter {

    private final FixedTimeMap<String, CounterData> counterMap = new FixedTimeMap<>();
    private final FixedTimeMap<String, AtomicLong> blacklistMap = new FixedTimeMap<>();
    private final FixedTimeMap<String, Long> whitelistMap = new FixedTimeMap<>();

    @Override
    public long incrementAndGetNormalRequestCount(String key, TimeUnit timeUnit, long duration) {
        return counterMap
                .getOrCreate(key, k -> new CounterData(), timeUnit, duration)
                .incrementAndGet(timeUnit, duration);
    }

    @Override
    public void blacklist(String key, TimeUnit timeUnit, long duration) {
        blacklistMap.getOrCreate(key, s -> new AtomicLong(), timeUnit, duration);
    }

    @Deprecated
    public boolean isBlacklisted(String key) {
        return incrementAndGetBlacklistedRequestCount(key) != 0L;
    }

    @Override
    public long incrementAndGetBlacklistedRequestCount(String key) {
        final AtomicLong counterData = blacklistMap.get(key);
        if (counterData == null) {
            return 0L;
        }

        return counterData.incrementAndGet();
    }

    @Override
    public void whitelist(String key, TimeUnit timeUnit, long duration) {
        whitelistMap.getOrCreate(key, s -> 1L, timeUnit, duration);
    }

    @Override
    public boolean isWhitelisted(String key) {
        return whitelistMap.get(key) != null;
    }

    public Statistic getStatistic() {
        return new Statistic(
                getStatisticEntriesCounter(counterMap),
                getStatisticEntries(whitelistMap),
                getStatisticEntriesAtomicLong(blacklistMap)
        );
    }

    private static List<StatisticEntry> getStatisticEntries(FixedTimeMap<String, Long> map) {
        return map
                .entrySet()
                .stream()
                .map(e -> new StatisticEntry(e.getKey(), e.getValue().getEndTime(), null))
                .collect(Collectors.toList());
    }

    private static List<StatisticEntry> getStatisticEntriesAtomicLong(FixedTimeMap<String, AtomicLong> map) {
        return map
                .entrySet()
                .stream()
                .map(e -> new StatisticEntry(e.getKey(), e.getValue().getEndTime(), e.getValue().getValue().get()))
                .collect(Collectors.toList());
    }

    private static List<StatisticEntry> getStatisticEntriesCounter(FixedTimeMap<String, CounterData> map) {
        return map
                .entrySet()
                .stream()
                .map(e -> new StatisticEntry(e.getKey(), e.getValue().getEndTime(), e.getValue().getValue().requests.longValue()))
                .collect(Collectors.toList());
    }

    private static class CounterData {

        private final AtomicLong requests = new AtomicLong();
        private final AtomicLong startTime;

        private CounterData() {
            startTime = new AtomicLong(System.currentTimeMillis());
        }

        private long incrementAndGet(TimeUnit timeUnit, long duration) {
            long currentTimeMillis = System.currentTimeMillis();
            long duration1 = currentTimeMillis - startTime.longValue();
            if (duration1 > timeUnit.toMillis(duration)) {
                reset(currentTimeMillis);
                return 1;
            } else {
                return requests.incrementAndGet();
            }
        }

        private void reset(long currentTimeMillis) {
            requests.set(1);
            startTime.set(currentTimeMillis);
        }
    }
}
