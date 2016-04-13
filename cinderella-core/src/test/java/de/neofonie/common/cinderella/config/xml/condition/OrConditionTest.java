package de.neofonie.common.cinderella.config.xml.condition;

import org.easymock.EasyMock;
import org.springframework.mock.web.MockHttpServletRequest;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class OrConditionTest {

    @Test
    public void testMatches() throws Exception {
        OrCondition orCondition = new OrCondition();
        MockHttpServletRequest request = new MockHttpServletRequest();

        orCondition.setConditions(null);
        assertFalse(orCondition.matches(request));

        orCondition.setConditions(new ArrayList<>());
        assertFalse(orCondition.matches(request));

        Condition yes = EasyMock.createMock(Condition.class);
        EasyMock.expect(yes.matches(request)).andReturn(true).anyTimes();
        Condition no = EasyMock.createMock(Condition.class);
        EasyMock.expect(no.matches(request)).andReturn(false).anyTimes();
        EasyMock.replay(yes, no);

        orCondition.setConditions(Arrays.asList(yes));
        assertTrue(orCondition.matches(request));

        orCondition.setConditions(Arrays.asList(no));
        assertFalse(orCondition.matches(request));

        orCondition.setConditions(Arrays.asList(yes, no));
        assertTrue(orCondition.matches(request));

        orCondition.setConditions(Arrays.asList(no, yes));
        assertTrue(orCondition.matches(request));
    }
}