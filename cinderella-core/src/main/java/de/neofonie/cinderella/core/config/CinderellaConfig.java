package de.neofonie.cinderella.core.config;

import de.neofonie.cinderella.core.config.xml.Rule;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface CinderellaConfig {

    List<Rule> getMatches(HttpServletRequest httpServletRequest);

    long getBlacklistMinutes();

    long getWhitelistMinutes();
}
