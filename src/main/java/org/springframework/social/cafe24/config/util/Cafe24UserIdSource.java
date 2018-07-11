package org.springframework.social.cafe24.config.util;

import com.cafe24.devbit004.pop.social.connect.Cafe24OAuth2Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.social.UserIdSource;

public class Cafe24UserIdSource implements UserIdSource {

    private static final Logger logger = LoggerFactory.getLogger(Cafe24UserIdSource.class);
    @Override
    public String getUserId() {
        logger.info("getUserId called...");
        return Cafe24OAuth2Template.getMallId();
    }
}
