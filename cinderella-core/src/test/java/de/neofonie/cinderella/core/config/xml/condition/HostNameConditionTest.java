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

package de.neofonie.cinderella.core.config.xml.condition;

import org.springframework.mock.web.MockHttpServletRequest;
import org.testng.annotations.Test;

import java.util.regex.Pattern;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class HostNameConditionTest {

    @Test
    public void testMatches() throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest();
        HostNameCondition google = createHostNameCondition("googlebot.com$");
        HostNameCondition sistrix = createHostNameCondition("sistrix.net$");
        HostNameCondition fooo = createHostNameCondition("fooo.com$");
        HostNameCondition localhost = createHostNameCondition("localhost");

        final String sistrixIP = "85.10.196.88";

        request.setRemoteAddr("127.0.0.1");
        assertFalse(google.matches(request));
        assertFalse(fooo.matches(request));
        assertFalse(sistrix.matches(request));
        assertTrue(localhost.matches(request));

        request.setRemoteAddr("66.249.66.1");
        assertTrue(google.matches(request));
        assertFalse(sistrix.matches(request));
        assertFalse(fooo.matches(request));


        request.setRemoteAddr(sistrixIP);
        sistrix.setIgnoreForwardDnsLookup(false);
        assertFalse(google.matches(request));
        assertFalse(fooo.matches(request));
        assertFalse(sistrix.matches(request));

        request.setRemoteAddr(sistrixIP);
        sistrix.setIgnoreForwardDnsLookup(true);
        assertTrue(sistrix.matches(request));
        assertFalse(google.matches(request));
        assertFalse(fooo.matches(request));
    }

    private static HostNameCondition createHostNameCondition(String pattern) {
        final HostNameCondition hostNameCondition = new HostNameCondition();
        hostNameCondition.setRegex(Pattern.compile(pattern));
        return hostNameCondition;
    }
}