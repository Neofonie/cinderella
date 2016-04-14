package de.neofonie.cinderella.core.config.loader;

import de.neofonie.cinderella.core.config.CinderellaConfig;

import java.io.IOException;

public interface CinderellaXmlConfigLoader {

    CinderellaConfig getCinderellaConfig() throws IOException;
}
