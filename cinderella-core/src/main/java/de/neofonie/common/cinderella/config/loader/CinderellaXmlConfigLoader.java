package de.neofonie.common.cinderella.config.loader;

import de.neofonie.common.cinderella.config.CinderellaConfig;

import java.io.IOException;

public interface CinderellaXmlConfigLoader {

    CinderellaConfig getCinderellaConfig() throws IOException;
}
