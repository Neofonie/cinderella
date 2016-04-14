package de.neofonie.cinderella.core.config.xml.condition;

import org.easymock.EasyMock;
import org.springframework.mock.web.MockHttpServletRequest;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class AndConditionTest {

    @Test
    public void testMatches() throws Exception {
        AndCondition andCondition = new AndCondition();
        MockHttpServletRequest request = new MockHttpServletRequest();

        andCondition.setConditions(new ArrayList<>());
        assertTrue(andCondition.matches(request));

        Condition yes = EasyMock.createMock(Condition.class);
        EasyMock.expect(yes.matches(request)).andReturn(true).anyTimes();
        Condition no = EasyMock.createMock(Condition.class);
        EasyMock.expect(no.matches(request)).andReturn(false).anyTimes();
        EasyMock.replay(yes, no);

        andCondition.setConditions(Arrays.asList(yes));
        assertTrue(andCondition.matches(request));

        andCondition.setConditions(Arrays.asList(no));
        assertFalse(andCondition.matches(request));

        andCondition.setConditions(Arrays.asList(yes, no));
        assertFalse(andCondition.matches(request));

        andCondition.setConditions(Arrays.asList(no, yes));
        assertFalse(andCondition.matches(request));
    }
}