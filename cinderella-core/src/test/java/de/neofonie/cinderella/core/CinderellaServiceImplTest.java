package de.neofonie.cinderella.core;

import de.neofonie.cinderella.core.config.CinderellaConfig;
import de.neofonie.cinderella.core.config.loader.CinderellaXmlConfigLoader;
import de.neofonie.cinderella.core.config.xml.IdentifierType;
import de.neofonie.cinderella.core.config.xml.Rule;
import de.neofonie.cinderella.core.counter.Counter;
import org.easymock.EasyMock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertEquals;

@ContextConfiguration(classes = CinderellaServiceImplTest.Config.class)
public class CinderellaServiceImplTest extends AbstractTestNGSpringContextTests {

    private static final TestCounter TEST_COUNTER = new TestCounter();
    @Autowired
    private CinderellaServiceImpl ddosService;
    @Autowired
    private CinderellaXmlConfigLoader cinderellaXmlConfigLoader;

    @Test
    public void testIsDdos() throws Exception {

        check(25, 0, 25, 5);
        check(50, 100, 50, 5);
        check(50, 5000, 50, 5);
        check(50, 5999, 50, 5);
        check(50, 6000, 50, 5);
        check(Integer.MAX_VALUE, 6001, 50, 5);
        check(Integer.MAX_VALUE, 8000, 50, 5);
        check(Integer.MAX_VALUE, 9000, 50, 5);
        check(Integer.MAX_VALUE, 10000, 50, 5);
        check(Integer.MAX_VALUE, 10999, 50, 5);
        check(Integer.MAX_VALUE, 11999, 50, 5);
        check(Integer.MAX_VALUE, 12000, 50, 5);
    }

    private void check(int expected, int wait, long requests, long minutes) throws Exception {
        CinderellaConfig cinderellaConfig = EasyMock.createMock(CinderellaConfig.class);
        TEST_COUNTER.reset(wait);
        EasyMock.reset(cinderellaXmlConfigLoader);
        EasyMock.expect(cinderellaXmlConfigLoader.getCinderellaConfig()).andReturn(cinderellaConfig).anyTimes();
        Rule rule = new Rule(new ArrayList<>(), "id", IdentifierType.IP, requests, minutes);
        EasyMock.expect(cinderellaConfig.getMatches(EasyMock.anyObject(MockHttpServletRequest.class)))
                .andReturn(Arrays.asList(rule)).anyTimes();
        EasyMock.expect(cinderellaConfig.getBlacklistMinutes()).andReturn(1L).atLeastOnce();

        EasyMock.replay(cinderellaConfig, cinderellaXmlConfigLoader);
        assertEquals(repeatUntilDdos(new MockHttpServletRequest()), expected);
    }

    @Test
    public void testWhitelist() throws Exception {
        CinderellaConfig cinderellaConfig = EasyMock.createMock(CinderellaConfig.class);
        TEST_COUNTER.reset(5);
        EasyMock.reset(cinderellaXmlConfigLoader);
        EasyMock.expect(cinderellaXmlConfigLoader.getCinderellaConfig()).andReturn(cinderellaConfig).anyTimes();
        Rule rule = new Rule(new ArrayList<>(), "id", IdentifierType.IP, 2, 5);
        EasyMock.expect(cinderellaConfig.getMatches(EasyMock.anyObject(MockHttpServletRequest.class)))
                .andReturn(Arrays.asList(rule)).anyTimes();
        EasyMock.expect(cinderellaConfig.getWhitelistMinutes()).andReturn(1L).atLeastOnce();

        EasyMock.replay(cinderellaConfig, cinderellaXmlConfigLoader);
        MockHttpSession session = new MockHttpSession();
        session.changeSessionId();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setSession(session);
        ddosService.whitelist(request);
        assertEquals(repeatUntilDdos(request), Integer.MAX_VALUE);
    }

    private int repeatUntilDdos(HttpServletRequest request) {
        int i = 1;
        while (!ddosService.isDdos(request)) {
            i++;
            if (i > 10000) {
                return Integer.MAX_VALUE;
            }
        }
        return i;
    }

    @AfterMethod
    public void tearDown() throws Exception {
        EasyMock.verify(cinderellaXmlConfigLoader);
        EasyMock.reset(cinderellaXmlConfigLoader);
    }

    static class TestCounter implements Counter {
        private double count;
        private long waittime;
        private long diff;
        private String whitelist;

        TestCounter() {
            reset(5);
        }

        @Override
        public boolean checkCount(String key, long requests, TimeUnit timeUnit, long minutes) {
            count++;
            diff += waittime;
            if (diff > minutes * 60 * 1000) {
                //idle - restart the counter
                reset(waittime);
                return false;
            }
            if (count >= requests) {
                //ddos
                return true;
            }
            return false;
        }

        @Override
        public void whitelist(String key, TimeUnit timeUnit, long duration) {
            whitelist = key;
        }

        @Override
        public boolean isWhitelisted(String key) {
            return whitelist != null && whitelist.equals(key);
        }

        @Override
        public void blacklist(String key, TimeUnit timeUnit, long duration) {

        }

        @Override
        public boolean isBlacklisted(String key) {
            return false;
        }

        public void reset(long waittime) {
            count = 0;
            diff = 0;
            this.waittime = waittime;
        }
    }

    @Configuration
    static class Config {

        @Bean
        CinderellaServiceImpl ddosService() {
            return new CinderellaServiceImpl();
        }

        @Bean
        CinderellaXmlConfigLoader ddosXmlConfigLoader() {
            return EasyMock.createMock(CinderellaXmlConfigLoader.class);
        }

        @Bean
        Counter counter() {
            return TEST_COUNTER;
        }
    }
}