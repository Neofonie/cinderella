package de.neofonie.common.cinderella;

import javax.servlet.http.HttpServletRequest;

public interface CinderellaService {

    boolean isDdos(HttpServletRequest httpServletRequest);

    void whitelist(HttpServletRequest request);
}
