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

import de.neofonie.cinderella.core.captcha.CaptchaService;
import org.easymock.EasyMock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.servlet.FilterChain;

@ContextConfiguration(classes = CinderellaFilterTest.Config.class)
public class CinderellaFilterTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private CinderellaFilter cinderellaFilter;
    @Autowired
    private CinderellaService cinderellaService;

    @BeforeMethod
    public void setUp() throws Exception {
        EasyMock.reset(cinderellaService);
    }

    @Test
    public void testDoFilterDdos() throws Exception {
        MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();
        FilterChain filterChain = EasyMock.createMock(FilterChain.class);
        EasyMock.expect(cinderellaService.isDdos(mockHttpServletRequest)).andReturn(true);
        EasyMock.replay(filterChain, cinderellaService);

        cinderellaFilter.doFilter(mockHttpServletRequest, mockHttpServletResponse, filterChain);

        EasyMock.verify(filterChain, cinderellaService);
    }

    @Test
    public void testDoFilterNoDdos() throws Exception {
        MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();
        FilterChain filterChain = EasyMock.createMock(FilterChain.class);
        filterChain.doFilter(mockHttpServletRequest, mockHttpServletResponse);
        EasyMock.expect(cinderellaService.isDdos(mockHttpServletRequest)).andReturn(false);
        EasyMock.replay(filterChain, cinderellaService);

        cinderellaFilter.doFilter(mockHttpServletRequest, mockHttpServletResponse, filterChain);
        EasyMock.verify(filterChain, cinderellaService);
    }

    @Configuration
    static class Config {

        @Bean
        CinderellaFilter ddosFilter() {
            CinderellaFilter cinderellaFilter1 = new CinderellaFilter();
            cinderellaFilter1.setDdosJsp("ddosJsp");
            return cinderellaFilter1;
        }

        @Bean
        CinderellaService ddosService() {
            return EasyMock.createMock(CinderellaService.class);
        }

        @Bean
        CaptchaService captchaService() {
            CaptchaService mock = EasyMock.createMock(CaptchaService.class);
            EasyMock.replay(mock);
            return mock;
        }
    }
}