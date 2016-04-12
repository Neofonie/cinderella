package de.neofonie.common.cinderella.config;

import de.neofonie.common.cinderella.config.xml.Rule;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface CinderellaConfig {

    List<Rule> getMatches(HttpServletRequest httpServletRequest);

    long getBlacklistMinutes();

    long getWhitelistMinutes();
}
