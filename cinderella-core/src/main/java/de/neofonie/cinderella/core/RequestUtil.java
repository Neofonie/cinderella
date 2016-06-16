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
import java.util.Enumeration;
import java.util.Stack;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class RequestUtil {

    private static final Logger logger = LoggerFactory.getLogger(RequestUtil.class);
    private static final CacheLoader<String, String> HOSTNAME_CACHE_LOADER =
            new CacheLoader<String, String>() {
                public String load(String clientIpAddr) {
                    final String hostNameHelper = getHostNameHelper(clientIpAddr);
                    if (hostNameHelper == null) {
                        return UNKNOWN_HOST;
                    }
                    return hostNameHelper;
                }
            };
    private static final LoadingCache<String, String> HOSTNAME_CACHE =
            CacheBuilder
                    .newBuilder()
                    .maximumSize(10000)
                    .expireAfterWrite(2, TimeUnit.HOURS)
                    .build(HOSTNAME_CACHE_LOADER);
    private static final String UNKNOWN_HOST = "";
    private static final Pattern COMPILE = Pattern.compile(",");
    private static Pattern PATERN_IP_ADDRESS = Pattern.compile(
            "^[ \\t]*(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})[, \\t]*$");

    private static Pattern PATTERN_IP_ADDRESSES = Pattern.compile(
            "(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})");


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
        final String clientIpAddrHelper = getClientIpAddrHelper(request);
        if (clientIpAddrHelper == null) {
            return null;
        }
        return getPublicIpByForwardedString(clientIpAddrHelper);
    }

    private static String getPublicIpByForwardedString(String ipAddresses) {
        if (ipAddresses == null) {
            return null;
        }

        final Matcher matcher = PATTERN_IP_ADDRESSES.matcher(ipAddresses);

        // We have to check the addresses from back to front
        // but the matcher matches from left to right
        // ... hence a stack
        // 129.33.22.3, 10.2.1.1, 192.168.1.2 --> so 192.168.1.2 first

        Stack<String> stackOfAddresses = new Stack<String>();

        while (matcher.find()) {
            stackOfAddresses.push(matcher.group(1).trim());
        }

        String ret = null;
        while (!stackOfAddresses.empty()) {
            ret = stackOfAddresses.pop().trim();
            if (!isPrivateIP(ret)) {
                return ret;
            }
        }
        return null;
    }

    private static boolean isPrivateIP(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }

        final Matcher matcher = PATERN_IP_ADDRESS.matcher(ip);
        if (matcher.matches()) {
            int first = Integer.parseInt(matcher.group(1));
            int second = Integer.parseInt(matcher.group(2));

            return
                    // 10.0.0.0 - 10.255.255.255
                    first == 10
                            // 172.16.0.0 - 172.31.255.255
                            || first == 172 && second >= 16 && second <= 31
                            // 192.168.0.0 - 192.168.255.255
                            || first == 192 && second == 168
                            // 224.x.x.x
                            || first == 224
                            // 169.254.x.x
                            || first == 169 && second == 254;
        } else {
            return true;
        }
    }

    private static String getClientIpAddrHelper(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
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
                logger.debug(String.format("%s - %d ms", clientIpAddr, TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start)));
                return null;
            }
            final String hostAddress = InetAddress.getByName(canonicalHostName).getHostAddress();
            if (clientIpAddr.equals(hostAddress)) {
                logger.debug(String.format("%s - %d ms", clientIpAddr, TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start)));
                return canonicalHostName;
            }
            logger.debug(String.format("%s - %d ms", clientIpAddr, TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start)));
            return null;
        } catch (UnknownHostException e) {
            logger.debug(e.getMessage());
            return null;
        }
    }

    public static boolean matchHeader(HttpServletRequest request, String headerName, Pattern pattern) {
        Enumeration<String> headers = request.getHeaders(headerName);
        if (headers == null) {
            return false;
        }
        while (headers.hasMoreElements()) {
            String v = headers.nextElement();
            if (pattern.matcher(v).find()) {
                return true;
            }
        }
        return false;
    }
}
