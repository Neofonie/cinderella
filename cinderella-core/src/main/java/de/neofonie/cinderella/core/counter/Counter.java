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
