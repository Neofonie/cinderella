package de.neofonie.common.cinderella.counter;

import java.util.List;

public class Statistic {

    private final List<StatisticEntry> counter;
    private final List<StatisticEntry> whitelist;
    private final List<StatisticEntry> blacklist;

    public Statistic(List<StatisticEntry> counter, List<StatisticEntry> whitelist, List<StatisticEntry> blacklist) {
        this.counter = counter;
        this.whitelist = whitelist;
        this.blacklist = blacklist;
    }

    public List<StatisticEntry> getCounter() {
        return counter;
    }

    public List<StatisticEntry> getWhitelist() {
        return whitelist;
    }

    public List<StatisticEntry> getBlacklist() {
        return blacklist;
    }
}
