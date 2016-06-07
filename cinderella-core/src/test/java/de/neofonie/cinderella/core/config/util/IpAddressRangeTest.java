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

package de.neofonie.cinderella.core.config.util;

import org.testng.Assert;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class IpAddressRangeTest {

    @Test
    public void testValueOf() throws Exception {
        fail("'null' is not a valid IP-Range", null);
        fail("'' is not a valid IP-Range", "");
//        success("localhost/127.0.0.1", "");
        fail("'foo' is not a valid IP-Range", "foo");
        fail("'1271.0.0.1' is not a valid IP-Range", "1271.0.0.1");
        fail("'127.0.0.256' is not a valid IP-Range", "127.0.0.256");

        success("127.0.0.1-127.0.0.255", "127.0.0.1-127.0.0.255");
        success("127.0.0.255", "127.0.0.255");

        //Test IP6
        success("2001:db8:85a3:8d3:1319:8a2e:370:7344",
                "2001:0db8:85a3:08d3:1319:8a2e:0370:7344");
        success("2001:db8:85a3:8d3:1319:8a2e:370:7344-2001:db8:85a3:9d3:1319:8a2e:370:7344",
                "2001:0db8:85a3:08d3:1319:8a2e:0370:7344-2001:0db8:85a3:09d3:1319:8a2e:0370:7344");
        success("127.0.0.1", "::ffff:7f00:1");
        success("127.0.0.1", "::ffff:127.0.0.1");

        //Is this useful???
        fail("'127.0.0.1-www.google.de' is not a valid IP-Range", "127.0.0.1-www.google.de");

        //Test mixed
        success("127.0.0.1", "127.0.0.1-::ffff:127.0.0.1");
        success("127.0.0.1", "::ffff:127.0.0.1-127.0.0.1");
        fail("'2001:db8:85a3:8d3:1319:8a2e:370:7344-127.0.0.1' is not a valid IP-Range", "2001:db8:85a3:8d3:1319:8a2e:370:7344-127.0.0.1");
        fail("'127.0.0.1-2001:db8:85a3:8d3:1319:8a2e:370:7344' is not a valid IP-Range", "127.0.0.1-2001:db8:85a3:8d3:1319:8a2e:370:7344");
    }

    private static void fail(String expected, String value) {
        try {
            IpAddressRange ipAddressRange = IpAddressRange.valueOf(value);
            Assert.fail(value + " should fail - resolve to " + ipAddressRange);
        } catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), expected);
        }
    }

    private static void success(String expected, String value) {
        assertEquals(IpAddressRange.valueOf(value).toString(), expected);
    }

    @Test
    public void testContains() throws Exception {
        try {
            assertTrue(IpAddressRange.valueOf("127.0.0.1-127.0.0.255").contains(null));
            Assert.fail();
        } catch (NullPointerException e) {

        }
        assertTrue(IpAddressRange.valueOf("127.0.0.1-127.0.0.255").contains(IpAddress.valueOf("127.0.0.1")));
        assertTrue(IpAddressRange.valueOf("127.0.0.1-127.0.0.255").contains(IpAddress.valueOf("127.0.0.5")));
        assertTrue(IpAddressRange.valueOf("127.0.0.1-127.0.0.255").contains(IpAddress.valueOf("127.0.0.255")));
        assertFalse(IpAddressRange.valueOf("127.0.0.1-127.0.0.255").contains(IpAddress.valueOf("127.1.0.255")));
        assertFalse(IpAddressRange.valueOf("127.0.0.1-127.0.0.255").contains(IpAddress.valueOf("126.0.0.1")));
        assertFalse(IpAddressRange.valueOf("127.0.0.1-127.0.0.255").contains(IpAddress.valueOf("128.0.0.1")));

        assertTrue(IpAddressRange.valueOf("127.0.0.1").contains(IpAddress.valueOf("127.0.0.1")));
        assertFalse(IpAddressRange.valueOf("127.0.0.1").contains(IpAddress.valueOf("127.0.0.2")));
        assertFalse(IpAddressRange.valueOf("127.0.0.1").contains(IpAddress.valueOf("127.0.0.0")));

        //Test IP6
        assertTrue(IpAddressRange.valueOf("2001:0db8:85a3:08d3:1319:8a2e:0370:7344-2001:0db8:85a3:09d3:1319:8a2e:0370:7344")
                .contains(IpAddress.valueOf("2001:0db8:85a3:09d3:1319:8a2e:0370:7344")));
        assertFalse(IpAddressRange.valueOf("2001:0db8:85a3:08d3:1319:8a2e:0370:7344-2001:0db8:85a3:09d3:1319:8a2e:0370:7344")
                .contains(IpAddress.valueOf("2001:0db8:85a3:08d3:1319:8a2e:0370:7343")));
        assertFalse(IpAddressRange.valueOf("2001:0db8:85a3:08d3:1319:8a2e:0370:7344-2001:0db8:85a3:09d3:1319:8a2e:0370:7344")
                .contains(IpAddress.valueOf("2001:0db8:85a3:f8d3:1319:8a2e:0370:7344")));

        //Test mixed
        assertFalse(IpAddressRange.valueOf("127.0.0.1-127.0.0.255").contains(IpAddress.valueOf("2001:0db8:85a3:09d3:1319:8a2e:0370:7344")));
        assertFalse(IpAddressRange.valueOf("2001:0db8:85a3:08d3:1319:8a2e:0370:7344-2001:0db8:85a3:09d3:1319:8a2e:0370:7344")
                .contains(IpAddress.valueOf("127.0.0.1")));

// local IPs - https://de.wikipedia.org/wiki/Private_IP-Adresse
        assertTrue(IpAddressRange.valueOf("10.0.0.0-10.255.255.255").contains(IpAddress.valueOf("10.32.124.211")));
        assertTrue(IpAddressRange.valueOf("172.16.0.0-172.31.255.255").contains(IpAddress.valueOf("172.24.10.5")));
        assertTrue(IpAddressRange.valueOf("192.168.0.0-192.168.255.255").contains(IpAddress.valueOf("192.168.0.1")));
    }
}