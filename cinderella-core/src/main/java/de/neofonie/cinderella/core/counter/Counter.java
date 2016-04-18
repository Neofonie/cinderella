package de.neofonie.cinderella.core.counter;

import java.util.concurrent.TimeUnit;

public interface Counter {

    /**
     * Increment the counter and returns true, if more than x requests for this key for the given duration.
     *
     * @param key
     * @param requests
     * @param timeUnit
     * @param duration
     */
    boolean checkCount(String key, long requests, TimeUnit timeUnit, long duration);

    /**
     * Sets a key as whitelist for the given duration
     * @param key
     * @param requests
     * @param timeUnit
     * @param duration
     * @return
     */
    void whitelist(String key, TimeUnit timeUnit, long duration);

    /**
     * returns true, if the key is whitelisted
     * @param key
     * @return
     */
    boolean isWhitelisted(String key);

    /**
     * Sets a key as blacklist for the given duration
     * @param key
     * @param requests
     * @param timeUnit
     * @param duration
     * @return
     */
    void blacklist(String key, TimeUnit timeUnit, long duration);

    /**
     * returns true, if the key is blacklisted
     * @param key
     * @return
     */
    boolean isBlacklisted(String key);
}
