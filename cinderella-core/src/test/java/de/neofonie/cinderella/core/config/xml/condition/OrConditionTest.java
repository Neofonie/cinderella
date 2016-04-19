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