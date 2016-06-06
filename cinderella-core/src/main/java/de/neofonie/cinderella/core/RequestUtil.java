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

package de.neofonie.cinderella.core;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public final class RequestUtil {

    private static final Logger logger = LoggerFactory.getLogger(RequestUtil.class);
    private final static CacheLoader<String, String> HOSTNAME_CACHE_LOADER =
            new CacheLoader<String, String>() {
                public String load(String clientIpAddr) {
                    final String hostNameHelper = getHostNameHelper(clientIpAddr);
                    if (hostNameHelper == null) {
                        return UNKNOWN_HOST;
                    }
                    return hostNameHelper;
                }
            };
    private final static LoadingCache<String, String> HOSTNAME_CACHE =
            CacheBuilder
                    .newBuilder()
                    .maximumSize(1000)
                    .expireAfterWrite(30, TimeUnit.MINUTES)
                    .build(HOSTNAME_CACHE_LOADER);
    public static final String UNKNOWN_HOST = "";

    private RequestUtil() {
    }

    public static String getSessionId(HttpServletRequest request) {
        HttpSession session = getSession(request);
        return session != null ? session.getId() : null;
    }

    public static boolean hasSession(HttpServletRequest request) {
        final HttpSession session = getSession(request);
        return session != null && !session.isNew();
    }

    public static HttpSession getSession(HttpServletRequest request) {
        return request.getSession(false);
    }

    public static String getClientIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }
        ip = request.getHeader("Proxy-Client-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }
        ip = request.getHeader("WL-Proxy-Client-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }
        ip = request.getHeader("HTTP_CLIENT_IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }
        ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }
        return request.getRemoteAddr();
    }

    public static String getHostName(HttpServletRequest request) {
        final String clientIpAddr = getClientIpAddr(request);
        if (clientIpAddr == null) {
            return null;
        }
        try {
            final String host = HOSTNAME_CACHE.get(clientIpAddr);
            if (UNKNOWN_HOST.equals(host)) {
                return null;
            }
            return host;
        } catch (ExecutionException e) {
            logger.error("", e);
            return null;
        }
    }

    static String getHostNameHelper(final String clientIpAddr) {

        if (clientIpAddr == null) {
            return null;
        }

        try {
            final long start = System.nanoTime();
            InetAddress ia = InetAddress.getByName(clientIpAddr);
            final String canonicalHostName = ia.getCanonicalHostName();
            if (canonicalHostName == null) {
                logger.info(String.format("%s - %d ms", clientIpAddr, TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start)));
                return null;
            }
            final String hostAddress = InetAddress.getByName(canonicalHostName).getHostAddress();
            if (clientIpAddr.equals(hostAddress)) {
                logger.info(String.format("%s - %d ms", clientIpAddr, TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start)));
                return canonicalHostName;
            }
            logger.info(String.format("%s - %d ms", clientIpAddr, TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start)));
            return null;
        } catch (UnknownHostException e) {
            logger.info(e.getMessage());
            return null;
        }
    }
}
