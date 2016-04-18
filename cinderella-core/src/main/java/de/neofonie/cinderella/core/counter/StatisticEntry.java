package de.neofonie.cinderella.core.counter;

import java.util.Date;

public class StatisticEntry {

    private final String key;
    private final Date validUntil;
    private final Long count;

    public StatisticEntry(String key, long validUntil, Long count) {
        this.key = key;
        this.validUntil = new Date(validUntil);
        this.count = count;
    }

    public String getKey() {
        return key;
    }

    public Date getValidUntil() {
        return validUntil;
    }

    public Long getCount() {
        return count;
    }
}