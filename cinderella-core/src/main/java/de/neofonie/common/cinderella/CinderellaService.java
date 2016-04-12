package de.neofonie.common.cinderella;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by paasch on 05.04.16.
 */
public interface CinderellaService {

    boolean isDdos(HttpServletRequest httpServletRequest);

    void whitelist(HttpServletRequest request);
}
