package org.springframework.social.cafe24.config.util;

import com.cafe24.devbit004.pop.social.api.impl.Cafe24Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionSignUp;

public class ProviderUserIdConnectionSignUp implements ConnectionSignUp {
    private static final Logger logger = LoggerFactory.getLogger(ProviderUserIdConnectionSignUp.class);
    @Override
    public String execute(Connection<?> connection) {
        logger.info("execute called...");
        String providerUserId = ((Cafe24Template)connection.getApi()).getMallId();
        logger.info("providerUserId: " + providerUserId);

        return providerUserId;
    }
}
