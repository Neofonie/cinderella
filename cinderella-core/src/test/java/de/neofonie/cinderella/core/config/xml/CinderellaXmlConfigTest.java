package de.neofonie.cinderella.core.config.xml;

import de.neofonie.cinderella.core.config.xml.condition.Condition;
import org.easymock.EasyMock;
import org.springframework.mock.web.MockHttpServletRequest;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.assertEquals;

public class CinderellaXmlConfigTest {

    @Test
    public void testGetMatches() throws Exception {
        CinderellaXmlConfig cinderellaXmlConfig = new CinderellaXmlConfig();
        assertMatch(cinderellaXmlConfig);

        cinderellaXmlConfig.setRules(Arrays.asList(
                new Rule(Arrays.asList(), "1", IdentifierType.IP, 1, 2),
                new Rule(Arrays.asList(), "2", IdentifierType.IP, 1, 2),
                new Rule(Arrays.asList(), "3", IdentifierType.SESSION, 1, 2)
        ));

        assertMatch(cinderellaXmlConfig, "1", "2");

        cinderellaXmlConfig.setWhitelist(new Whitelist(Arrays.<Condition>asList()));
        assertMatch(cinderellaXmlConfig, "1", "2");

        Condition condition = EasyMock.createMock(Condition.class);
        EasyMock.expect(condition.matches(EasyMock.anyObject())).andReturn(true).anyTimes();
        EasyMock.replay(condition);
        cinderellaXmlConfig.setWhitelist(new Whitelist(Arrays.<Condition>asList(condition)));
        assertMatch(cinderellaXmlConfig);
    }

    private void assertMatch(CinderellaXmlConfig cinderellaXmlConfig, String... ids) {
        List<Rule> matches = cinderellaXmlConfig.getMatches(new MockHttpServletRequest());
        assertEquals(matches.size(), ids.length);
        for (int i = 0; i < matches.size(); i++) {
            assertEquals(matches.get(i).getId(), ids[i]);
        }
    }
}