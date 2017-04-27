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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class CinderellaServiceImpl implements CinderellaService {

    private static final Logger logger = LoggerFactory.getLogger(CinderellaServiceImpl.class);
    @Autowired
    private CinderellaXmlConfigLoader cinderellaXmlConfigLoader;
    @Autowired
    private Counter counter;

    @Override
    public ActionEnum getAction(HttpServletRequest request) {

        final String clientIpAddr = RequestUtil.getClientIpAddr(request);
        if (isWhitelisted(request)) {
            return ActionEnum.NO_DDOS;
        }
        CinderellaConfig cinderellaConfig = cinderellaXmlConfigLoader.getCinderellaConfig();
        if (cinderellaConfig == null) {
            return ActionEnum.NO_DDOS;
        }

        long blacklistCount = getBlacklistCount(request);

        if (blacklistCount > 0L) {
            if (isDDOS(request, this::incrementBlacklistCount)) {
                blacklistCount = getBlacklistCount(request);
            }
            final long noResponseThreshould = cinderellaConfig.getNoResponseThreshould();
            if (noResponseThreshould > 0L && blacklistCount >= noResponseThreshould) {
                logger.debug(String.format("%s blacklisted (%d) and no response", clientIpAddr, blacklistCount));
                return ActionEnum.NO_RESPONSE;
            } else {
                logger.debug(String.format("%s blacklisted (%d)", clientIpAddr, blacklistCount));
                return ActionEnum.DDOS;
            }
        }
        if (isDDOS(request, this::blacklist)) {
            return ActionEnum.DDOS;
        }
        return ActionEnum.NO_DDOS;
    }

    protected boolean isDDOS(HttpServletRequest request, Consumer<HttpServletRequest> action) {
        CinderellaConfig cinderellaConfig = cinderellaXmlConfigLoader.getCinderellaConfig();
        List<Rule> matches = cinderellaConfig.getMatches(request);
        for (Rule rule : matches) {
            String key = rule.getRuleKey(request);
            long count = counter.incrementAndGetNormalRequestCount(key, TimeUnit.MINUTES, rule.getMinutes());
            boolean ddos = count >= rule.getRequests();
            if (ddos) {
                action.accept(request);
                return true;
            }
        }
        return false;
    }

    protected void blacklist(HttpServletRequest request) {
        CinderellaConfig cinderellaConfig = cinderellaXmlConfigLoader.getCinderellaConfig();
        List<Rule> matches = cinderellaConfig.getMatches(request);
        for (Rule rule : matches) {
            final String identifier = rule.getIdentifierType().getIdentifier(request);
            counter.blacklist(identifier, TimeUnit.MINUTES, cinderellaConfig.getBlacklistMinutes());
            break;
        }
    }

    protected boolean isWhitelisted(HttpServletRequest request) {
        if (IdentifierType.SESSION.accept(request)) {
            String identifier = IdentifierType.SESSION.getIdentifier(request);
            if (counter.isWhitelisted(identifier)) {
                return true;
            }
        }
        return false;
    }

    protected void incrementBlacklistCount(HttpServletRequest request) {
        for (IdentifierType identifierType : IdentifierType.values()) {
            if (identifierType.accept(request)) {
                String key = identifierType.getIdentifier(request);
                counter.incrementAndGetBlacklistedRequestCount(key);
            }
        }
    }

    protected long getBlacklistCount(HttpServletRequest request) {
        long result = 0L;
        for (IdentifierType identifierType : IdentifierType.values()) {
            if (identifierType.accept(request)) {
                String key = identifierType.getIdentifier(request);
                final long count = counter.getBlacklistedRequestCount(key);
                if (count > result) {
                    result = count;
                }
            }
        }
        return result;
    }

    protected void resetBlacklistCount(HttpServletRequest request) {
        CinderellaConfig cinderellaConfig = cinderellaXmlConfigLoader.getCinderellaConfig();
        if (cinderellaConfig == null) {
            return;
        }
        List<Rule> matches = cinderellaConfig.getRules();
        for (IdentifierType identifierType : IdentifierType.values()) {
            counter.resetBlacklistCount(identifierType.getIdentifier(request));
        }
        for (Rule rule : matches) {
            String key = rule.getRuleKey(request);
            counter.resetCounter(key);
        }
    }

    @Override
    public void whitelist(HttpServletRequest request) {
        if (!IdentifierType.SESSION.accept(request)) {
            logger.info("whitelisting is only availaible with valid session");
            return;
        }
        String identifier = IdentifierType.SESSION.getIdentifier(request);

        CinderellaConfig cinderellaConfig = cinderellaXmlConfigLoader.getCinderellaConfig();
        if (cinderellaConfig == null) {
            return;
        }
        counter.whitelist(identifier, TimeUnit.MINUTES, cinderellaConfig.getWhitelistMinutes());
        resetBlacklistCount(request);
    }
}
