package de.neofonie.common.cinderella;

import de.neofonie.common.cinderella.config.CinderellaConfig;
import de.neofonie.common.cinderella.config.loader.CinderellaXmlConfigLoader;
import de.neofonie.common.cinderella.config.xml.IdentifierType;
import de.neofonie.common.cinderella.config.xml.Rule;
import de.neofonie.common.cinderella.counter.Counter;
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
                String key = rule.getIdentifierType().getIdentifier(request) + '_' + rule.getId();
                boolean ddos = counter.isDdos(key, rule.getRequests(), TimeUnit.MINUTES, rule.getMinutes());
                if (ddos) {
                    counter.blacklist(key, TimeUnit.MINUTES, cinderellaConfig.getBlacklistMinutes());
                    return true;
                }
            }
            return false;
        } catch (IOException e) {
            logger.error("", e);
            return false;
        }
    }

    private boolean isWhitelisted(HttpServletRequest request) {
        if (IdentifierType.SESSION.accept(request)) {
            String identifier = IdentifierType.SESSION.getIdentifier(request);
            if (counter.isWhitelisted(identifier)) {
                return true;
            }
        }
        return false;
    }

    private boolean isBlacklisted(HttpServletRequest request) {
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
