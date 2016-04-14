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
        success("www.google.de/172.217.21.3", "www.google.de");

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
        success("www.google.de/172.217.21.3-127.0.0.1", "www.google.de-127.0.0.1");
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

    }
}