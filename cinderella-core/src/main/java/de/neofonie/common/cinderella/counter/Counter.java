package de.neofonie.common.cinderella.counter;

import java.util.concurrent.TimeUnit;

public interface Counter {

    boolean isDdos(String key, long requests, TimeUnit timeUnit, long duration);

    void whitelist(String key, TimeUnit timeUnit, long duration);

    boolean isWhitelisted(String key);

    void blacklist(String key, TimeUnit timeUnit, long duration);

    boolean isBlacklisted(String key);
}
