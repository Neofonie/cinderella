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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class IpAddressTest {

    @Test
    public void testValueOf() throws Exception {
        fail("'null' is not a valid IP-Adress", null);
        fail("'' is not a valid IP-Adress", "");
        fail("'foo' is not a valid IP-Adress", "foo");
        fail("'1271.0.0.1' is not a valid IP-Adress", "1271.0.0.1");
        fail("'127.0.0.256' is not a valid IP-Adress", "127.0.0.256");
        success("127.0.0.1", "127.0.0.1");
        success("127.0.0.255", "127.0.0.255");

        //Test IP6
        success("2001:db8:85a3:8d3:1319:8a2e:370:7344", "2001:0db8:85a3:08d3:1319:8a2e:0370:7344");
        success("127.0.0.1", "::ffff:7f00:1");
        success("127.0.0.1", "::ffff:127.0.0.1");
    }

    private static void fail(String expected, String value) {
        try {
            IpAddress ipAddress = IpAddress.valueOf(value);
            Assert.fail(value + " should fail - resolve to " + ipAddress);
        } catch (IllegalArgumentException e) {
            assertEquals(expected, e.getMessage());
        }
    }

    private static void success(String expected, String value) {
        assertEquals(IpAddress.valueOf(value).toString(), expected);
    }

    @Test
    public void testIsLesserEqualThan() throws Exception {
        assertTrue(IpAddress.valueOf("127.0.0.1").isLesserEqualThan(IpAddress.valueOf("127.0.0.1")));
    }
}