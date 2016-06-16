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

import org.springframework.mock.web.MockHttpServletRequest;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class RequestUtilTest {

    @Test
    public void testIpAddress123() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("RemoteAddr");
//        request.addHeader("X-Forwarded-For", null);
        assertEquals(RequestUtil.getClientIpAddr(request), "RemoteAddr");
    }

    @Test
    public void testIpAddress() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("178.255.215.98");
        assertEquals(RequestUtil.getClientIpAddr(request), "178.255.215.98");
    }

    @Test
    public void testIpAddress2() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("178.255.215.98");
        request.addHeader("X-Forwarded-For", "");
        assertEquals(RequestUtil.getClientIpAddr(request), "178.255.215.98");
    }

    @Test
    public void testIpAddress3() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("RemoteAddr");
        request.addHeader("X-Forwarded-For", "66.249.66.1");
        assertEquals(RequestUtil.getClientIpAddr(request), "66.249.66.1");
        assertEquals(RequestUtil.getHostName(request), "crawl-66-249-66-1.googlebot.com");
    }

    @Test
    public void testIpAddress4() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("RemoteAddr");
        request.addHeader("X-Forwarded-For", "192.168.0.1,66.249.66.1");
        assertEquals(RequestUtil.getClientIpAddr(request), "66.249.66.1");
        assertEquals(RequestUtil.getHostName(request), "crawl-66-249-66-1.googlebot.com");
    }

    @Test
    public void testGetHostName() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");

        assertEquals(RequestUtil.getHostName(request), "localhost");

        request.setRemoteAddr("66.249.66.1");
        assertEquals(RequestUtil.getHostName(request), "crawl-66-249-66-1.googlebot.com");

        request.setRemoteAddr("66.247.66.1");
        assertEquals(RequestUtil.getHostName(request), "66.247.66.1");

        request.setRemoteAddr(null);
        assertEquals(RequestUtil.getHostName(request), null);

        request.setRemoteAddr("crap");
        assertEquals(RequestUtil.getHostName(request), null);
    }

    @Test
    public void testGetHostNameHelper() throws Exception {
        assertEquals(RequestUtil.getHostNameHelper("66.247.66.1"), "66.247.66.1");
        assertEquals(RequestUtil.getHostNameHelper("66.249.66.1"), "crawl-66-249-66-1.googlebot.com");
        //should be heise.de?
        assertEquals(RequestUtil.getHostNameHelper("193.99.144.80"), "193.99.144.80");

        assertEquals(RequestUtil.getHostNameHelper("127.0.0.1"), "localhost");
        assertEquals(RequestUtil.getHostNameHelper(null), null);
        assertEquals(RequestUtil.getHostNameHelper("crap"), null);
    }

    @Test
    public void testXforwardedForIpListWithGoogleAndBlanks() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("RemoteAddr");
        request.addHeader("X-Forwarded-For", " 193.99.144.80 , 66.249.66.1 ");
        assertEquals(RequestUtil.getClientIpAddr(request), "66.249.66.1");
        assertEquals(RequestUtil.getHostName(request), "crawl-66-249-66-1.googlebot.com");
    }

    @Test
    public void testXforwardedForIpListWithGoogle() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("RemoteAddr");
        request.addHeader("X-Forwarded-For", "193.99.144.80, 66.249.66.1");
        assertEquals(RequestUtil.getClientIpAddr(request), "66.249.66.1");
        assertEquals(RequestUtil.getHostName(request), "crawl-66-249-66-1.googlebot.com");
    }

    @Test
    public void testXforwardedForIpListWithGoogleAndPrivateIps() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("RemoteAddr");
        request.addHeader("X-Forwarded-For", " 193.99.144.80 , 66.249.66.1 , 172.16.0.0, 10.255.0.0, 192.168.0.0");
        assertEquals(RequestUtil.getClientIpAddr(request), "66.249.66.1");
        assertEquals(RequestUtil.getHostName(request), "crawl-66-249-66-1.googlebot.com");
    }

    @Test
    public void testXforwardedOnlyPrivateIps() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.4.5.6");
        request.addHeader("X-Forwarded-For", "172.16.0.0, 10.255.0.0, 192.168.0.0");
        assertEquals(RequestUtil.getClientIpAddr(request), "10.4.5.6");
//        assertEquals(RequestUtil.getHostName(request), "10.4.5.6");
    }
}