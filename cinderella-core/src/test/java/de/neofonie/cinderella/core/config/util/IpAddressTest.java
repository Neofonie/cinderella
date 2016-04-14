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
        success("www.google.de/172.217.21.3", "www.google.de");
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