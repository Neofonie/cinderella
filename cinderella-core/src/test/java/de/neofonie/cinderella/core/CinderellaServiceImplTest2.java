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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertEquals;

@ContextConfiguration(classes = CinderellaServiceImplTest2.Config.class)
public class CinderellaServiceImplTest2 extends AbstractTestNGSpringContextTests {

    @Autowired
    private CinderellaServiceImpl ddosService;
    @Autowired
    private Counter counter;
    @Autowired
    private CinderellaXmlConfigLoader cinderellaXmlConfigLoader;
    private static final Rule rule = new Rule(new ArrayList<>(), "id", IdentifierType.IP, 2, 5);
    private static final Rule notMatchingRule = new Rule(new ArrayList<>(), "ruleId2", IdentifierType.SESSION, 2, 5);

    @Test
    public void testWhitelistSession() throws Exception {

        CinderellaConfig cinderellaConfig = EasyMock.createMock(CinderellaConfig.class);
        MockHttpServletRequest request = createMockHttpServletRequest();

        //Init
        EasyMock.expect(counter.isWhitelisted("SESSION_ID")).andReturn(true);

        //Test
        EasyMock.replay(cinderellaConfig, cinderellaXmlConfigLoader, counter);
        assertEquals(ActionEnum.NO_DDOS, ddosService.getAction(request));
        EasyMock.verify(cinderellaConfig, cinderellaXmlConfigLoader, counter);
        EasyMock.reset(cinderellaConfig, cinderellaXmlConfigLoader, counter);
    }

    @Test
    public void testChecksBlacklistSession() throws Exception {

        CinderellaConfig cinderellaConfig = EasyMock.createMock(CinderellaConfig.class);
        MockHttpServletRequest request = createMockHttpServletRequest();

        //Init
        EasyMock.expect(counter.isWhitelisted("SESSION_ID")).andReturn(false);
        EasyMock.expect(cinderellaXmlConfigLoader.getCinderellaConfig()).andReturn(cinderellaConfig).times(2);
        EasyMock.expect(counter.getBlacklistedRequestCount("SESSION_ID")).andReturn(10L).times(2);
        EasyMock.expect(counter.getBlacklistedRequestCount("127.0.0.1")).andReturn(0L).times(2);
        EasyMock.expect(cinderellaConfig.getNoResponseThreshould()).andReturn(0L);
        EasyMock.expect(cinderellaConfig.getMatches(request)).andReturn(Arrays.asList(rule));
        EasyMock.expect(counter.incrementAndGetNormalRequestCount("127.0.0.1_id", TimeUnit.MINUTES, 5)).andReturn(10L);
        EasyMock.expect(counter.incrementAndGetBlacklistedRequestCount("SESSION_ID")).andReturn(10L);
        EasyMock.expect(counter.incrementAndGetBlacklistedRequestCount("127.0.0.1")).andReturn(0L);

        //Test
        EasyMock.replay(cinderellaConfig, cinderellaXmlConfigLoader, counter);
        assertEquals(ActionEnum.DDOS, ddosService.getAction(request));
        EasyMock.verify(cinderellaConfig, cinderellaXmlConfigLoader, counter);
        EasyMock.reset(cinderellaConfig, cinderellaXmlConfigLoader, counter);

    }

    @Test
    public void testChecksBlacklistIP() throws Exception {

        CinderellaConfig cinderellaConfig = EasyMock.createMock(CinderellaConfig.class);
        MockHttpServletRequest request = createMockHttpServletRequest();

        //Init
        EasyMock.expect(counter.isWhitelisted("SESSION_ID")).andReturn(false);
        EasyMock.expect(cinderellaXmlConfigLoader.getCinderellaConfig()).andReturn(cinderellaConfig).times(2);
        EasyMock.expect(counter.getBlacklistedRequestCount("SESSION_ID")).andReturn(0L).times(2);
        EasyMock.expect(counter.getBlacklistedRequestCount("127.0.0.1")).andReturn(10L).times(2);
        EasyMock.expect(cinderellaConfig.getNoResponseThreshould()).andReturn(0L);
        EasyMock.expect(cinderellaConfig.getMatches(request)).andReturn(Arrays.asList(rule));
        EasyMock.expect(counter.incrementAndGetNormalRequestCount("127.0.0.1_id", TimeUnit.MINUTES, 5)).andReturn(10L);
        EasyMock.expect(counter.incrementAndGetBlacklistedRequestCount("SESSION_ID")).andReturn(0L);
        EasyMock.expect(counter.incrementAndGetBlacklistedRequestCount("127.0.0.1")).andReturn(10L);


        //Test
        EasyMock.replay(cinderellaConfig, cinderellaXmlConfigLoader, counter);
        assertEquals(ActionEnum.DDOS, ddosService.getAction(request));
        EasyMock.verify(cinderellaConfig, cinderellaXmlConfigLoader, counter);
        EasyMock.reset(cinderellaConfig, cinderellaXmlConfigLoader, counter);
    }

    @Test
    public void testNoConfig() throws Exception {

        CinderellaConfig cinderellaConfig = EasyMock.createMock(CinderellaConfig.class);
        MockHttpServletRequest request = createMockHttpServletRequest();

        //Init
        EasyMock.expect(counter.isWhitelisted("SESSION_ID")).andReturn(false);
        EasyMock.expect(cinderellaXmlConfigLoader.getCinderellaConfig()).andReturn(null);

        //Test
        EasyMock.replay(cinderellaConfig, cinderellaXmlConfigLoader, counter);
        assertEquals(ActionEnum.NO_DDOS, ddosService.getAction(request));
        EasyMock.verify(cinderellaConfig, cinderellaXmlConfigLoader, counter);
        EasyMock.reset(cinderellaConfig, cinderellaXmlConfigLoader, counter);
    }

    @Test
    public void testNoMatch() throws Exception {

        CinderellaConfig cinderellaConfig = EasyMock.createMock(CinderellaConfig.class);
        MockHttpServletRequest request = createMockHttpServletRequest();

        //Init
        EasyMock.expect(counter.isWhitelisted("SESSION_ID")).andReturn(false);
        EasyMock.expect(cinderellaXmlConfigLoader.getCinderellaConfig()).andReturn(cinderellaConfig).times(2);
        EasyMock.expect(counter.getBlacklistedRequestCount("SESSION_ID")).andReturn(0L);
        EasyMock.expect(counter.getBlacklistedRequestCount("127.0.0.1")).andReturn(0L);
        EasyMock.expect(cinderellaConfig.getMatches(request)).andReturn(Collections.emptyList());

        //Test
        EasyMock.replay(cinderellaConfig, cinderellaXmlConfigLoader, counter);
        assertEquals(ActionEnum.NO_DDOS, ddosService.getAction(request));
        EasyMock.verify(cinderellaConfig, cinderellaXmlConfigLoader, counter);
        EasyMock.reset(cinderellaConfig, cinderellaXmlConfigLoader, counter);
    }

    @Test
    public void testNoDDos() throws Exception {

        CinderellaConfig cinderellaConfig = EasyMock.createMock(CinderellaConfig.class);
        MockHttpServletRequest request = createMockHttpServletRequest();
        assertEquals("SESSION_ID", request.getSession().getId());

        //Init
        EasyMock.expect(counter.isWhitelisted("SESSION_ID")).andReturn(false);
        EasyMock.expect(cinderellaXmlConfigLoader.getCinderellaConfig()).andReturn(cinderellaConfig).times(2);
        EasyMock.expect(counter.getBlacklistedRequestCount("SESSION_ID")).andReturn(0L);
        EasyMock.expect(counter.getBlacklistedRequestCount("127.0.0.1")).andReturn(0L);
        EasyMock.expect(cinderellaConfig.getMatches(request)).andReturn(Arrays.asList(rule));
        EasyMock.expect(counter.incrementAndGetNormalRequestCount("127.0.0.1_id", TimeUnit.MINUTES, 5)).andReturn(1L);

        //Test
        EasyMock.replay(cinderellaConfig, cinderellaXmlConfigLoader, counter);
        assertEquals(ActionEnum.NO_DDOS, ddosService.getAction(request));
        EasyMock.verify(cinderellaConfig, cinderellaXmlConfigLoader, counter);
        EasyMock.reset(cinderellaConfig, cinderellaXmlConfigLoader, counter);
    }

    @Test
    public void testSetBlacklist() throws Exception {

        CinderellaConfig cinderellaConfig = EasyMock.createMock(CinderellaConfig.class);
        MockHttpServletRequest request = createMockHttpServletRequest();

        //Init
        EasyMock.expect(cinderellaConfig.getBlacklistMinutes()).andReturn(1L);

        EasyMock.expect(counter.isWhitelisted("SESSION_ID")).andReturn(false);
        EasyMock.expect(cinderellaXmlConfigLoader.getCinderellaConfig()).andReturn(cinderellaConfig).times(3);
        EasyMock.expect(counter.getBlacklistedRequestCount("SESSION_ID")).andReturn(0L);
        EasyMock.expect(counter.getBlacklistedRequestCount("127.0.0.1")).andReturn(0L);
        EasyMock.expect(cinderellaConfig.getMatches(request)).andReturn(Arrays.asList(rule)).times(2);
        EasyMock.expect(counter.incrementAndGetNormalRequestCount("127.0.0.1_id", TimeUnit.MINUTES, 5)).andReturn(2L);
        counter.blacklist("127.0.0.1", TimeUnit.MINUTES, 1L);

        //Test
        EasyMock.replay(cinderellaConfig, cinderellaXmlConfigLoader, counter);
        assertEquals(ddosService.getAction(request), ActionEnum.DDOS);
        EasyMock.verify(cinderellaConfig, cinderellaXmlConfigLoader, counter);
        EasyMock.reset(cinderellaConfig, cinderellaXmlConfigLoader, counter);
    }

    @Test
    public void testSetWhitelist() throws Exception {

        CinderellaConfig cinderellaConfig = EasyMock.createMock(CinderellaConfig.class);
        MockHttpServletRequest request = createMockHttpServletRequest();

        //Init
        EasyMock.expect(cinderellaXmlConfigLoader.getCinderellaConfig()).andReturn(cinderellaConfig).atLeastOnce();
        counter.resetBlacklistCount("127.0.0.1");
        counter.resetBlacklistCount("SESSION_ID");
        counter.resetCounter("127.0.0.1_id");
        counter.resetCounter("SESSION_ID_ruleId2");
        EasyMock.expect(cinderellaConfig.getRules()).andReturn(Arrays.asList(rule, notMatchingRule));
        EasyMock.expect(cinderellaConfig.getWhitelistMinutes()).andReturn(5L);
        counter.whitelist("SESSION_ID", TimeUnit.MINUTES, 5L);

        //Test
        EasyMock.replay(cinderellaConfig, cinderellaXmlConfigLoader, counter);
        ddosService.whitelist(request);

        EasyMock.verify(cinderellaConfig, cinderellaXmlConfigLoader, counter);
        EasyMock.reset(cinderellaConfig, cinderellaXmlConfigLoader, counter);
    }

    private MockHttpServletRequest createMockHttpServletRequest() {
        MockHttpSession session = new MockHttpSession(null, "SESSION_ID");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setSession(session);
        return request;
    }

    @BeforeMethod
    public void setUp() throws Exception {
        EasyMock.reset(cinderellaXmlConfigLoader, counter);
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
            return EasyMock.createMock(Counter.class);
        }
    }
}