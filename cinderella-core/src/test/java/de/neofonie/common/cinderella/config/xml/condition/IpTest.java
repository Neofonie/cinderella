package de.neofonie.common.cinderella.config.xml.condition;

import de.neofonie.common.cinderella.config.util.IpAddressRange;
import org.springframework.mock.web.MockHttpServletRequest;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class IpTest {

    @Test
    public void testMatches() throws Exception {
        IpCondition ip = new IpCondition();
        ip.setIpAddressRange(IpAddressRange.valueOf("172.217.21.3"));
        MockHttpServletRequest request = new MockHttpServletRequest();
        assertFalse(ip.matches(request));

        request.setRemoteAddr("foobar");
        assertFalse(ip.matches(request));

        request.setRemoteAddr("172.217.21.3");
        assertTrue(ip.matches(request));
    }
}