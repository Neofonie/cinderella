package de.neofonie.cinderella.core;

import javax.servlet.http.HttpServletRequest;

public interface CinderellaService {

    boolean isDdos(HttpServletRequest httpServletRequest);

    void whitelist(HttpServletRequest request);
}
