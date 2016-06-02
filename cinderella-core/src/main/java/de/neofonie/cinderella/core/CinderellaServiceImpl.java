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
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CinderellaServiceImpl implements CinderellaService {

    private static final Logger logger = LoggerFactory.getLogger(CinderellaServiceImpl.class);
    @Autowired
    private CinderellaXmlConfigLoader cinderellaXmlConfigLoader;
    @Autowired
    private Counter counter;

    @Override
    public boolean isDdos(HttpServletRequest request) {

        try {
            if (isWhitelisted(request)) {
                return false;
            }
            if (isBlacklisted(request)) {
                return true;
            }
            CinderellaConfig cinderellaConfig = cinderellaXmlConfigLoader.getCinderellaConfig();
            if (cinderellaConfig == null) {
                return false;
            }
            List<Rule> matches = cinderellaConfig.getMatches(request);
            for (Rule rule : matches) {
                final String identifier = rule.getIdentifierType().getIdentifier(request);
                String key = identifier + '_' + rule.getId();
                boolean ddos = counter.checkCount(key, rule.getRequests(), TimeUnit.MINUTES, rule.getMinutes());
                if (ddos) {
                    blacklist(cinderellaConfig, rule, request);
                    return true;
                }
            }
            return false;
        } catch (IOException e) {
            logger.error("", e);
            return false;
        }
    }

    protected void blacklist(CinderellaConfig cinderellaConfig, Rule rule, HttpServletRequest request) {
        final String identifier = rule.getIdentifierType().getIdentifier(request);
        counter.blacklist(identifier, TimeUnit.MINUTES, cinderellaConfig.getBlacklistMinutes());
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

    protected boolean isBlacklisted(HttpServletRequest request) {
        for (IdentifierType identifierType : IdentifierType.values()) {
            if (identifierType.accept(request)) {
                String key = identifierType.getIdentifier(request);
                if (counter.isBlacklisted(key)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void whitelist(HttpServletRequest request) {
        if (!IdentifierType.SESSION.accept(request)) {
            logger.info("whitelisting is only availaible with valid session");
            return;
        }
        String identifier = IdentifierType.SESSION.getIdentifier(request);

        try {
            CinderellaConfig cinderellaConfig = cinderellaXmlConfigLoader.getCinderellaConfig();
            if (cinderellaConfig == null) {
                return;
            }
            counter.whitelist(identifier, TimeUnit.MINUTES, cinderellaConfig.getWhitelistMinutes());
        } catch (IOException e) {
            logger.error("", e);
        }
    }
}
