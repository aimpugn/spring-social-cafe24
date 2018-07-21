package org.springframework.social.cafe24.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.social.UserIdSource;
import org.springframework.social.cafe24.connect.Cafe24OAuth2Template;

public class Cafe24UserIdSource implements UserIdSource {

    private static final Logger logger = LoggerFactory.getLogger(Cafe24UserIdSource.class);
    @Override
    public String getUserId() {
        logger.info("getUserId called...");
        return Cafe24OAuth2Template.getMallId();
    }
}
