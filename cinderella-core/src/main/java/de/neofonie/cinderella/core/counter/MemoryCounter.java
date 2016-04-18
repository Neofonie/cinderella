package de.neofonie.cinderella.core.counter;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class MemoryCounter implements Counter {

    private final FixedTimeMap<String, CounterData> counterMap = new FixedTimeMap<>();
    private final FixedTimeMap<String, Long> blacklistMap = new FixedTimeMap<>();
    private final FixedTimeMap<String, Long> whitelistMap = new FixedTimeMap<>();

    @Override
    public boolean checkCount(String key, long requests, TimeUnit timeUnit, long duration) {
        long count = counterMap
                .getOrCreate(key, k -> new CounterData(), timeUnit, duration)
                .isDdos(timeUnit, duration);
        return count > requests;
    }

    @Override
    public void blacklist(String key, TimeUnit timeUnit, long duration) {
        blacklistMap.getOrCreate(key, s -> 1L, timeUnit, duration);
    }

    @Override
    public boolean isBlacklisted(String key) {
        return blacklistMap.get(key) != null;
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
                getStatisticEntries(blacklistMap)
        );
    }

    private static List<StatisticEntry> getStatisticEntries(FixedTimeMap<String, Long> map) {
        return map
                .entrySet()
                .stream()
                .map(e -> new StatisticEntry(e.getKey(), e.getValue().getEndTime(), null))
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

        private long isDdos(TimeUnit timeUnit, long duration) {
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
