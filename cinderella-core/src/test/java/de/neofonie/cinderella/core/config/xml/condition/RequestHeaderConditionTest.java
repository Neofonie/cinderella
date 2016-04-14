package de.neofonie.cinderella.core.config.xml.condition;

import org.springframework.mock.web.MockHttpServletRequest;
import org.testng.annotations.Test;

import java.util.regex.Pattern;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class RequestHeaderConditionTest {
    @Test
    public void testMatches() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();

        RequestHeaderCondition condition = new RequestHeaderCondition("foo", Pattern.compile("bar"));
        assertFalse(condition.matches(request));

        request.addHeader("foo", "bar");
        assertTrue(condition.matches(request));

        request.addHeader("foo", "bap");
        assertTrue(condition.matches(request));

        condition = new RequestHeaderCondition("foo", Pattern.compile("bap"));
        assertTrue(condition.matches(request));

    }
}