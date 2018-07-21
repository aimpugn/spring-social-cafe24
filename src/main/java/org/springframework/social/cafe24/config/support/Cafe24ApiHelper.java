package org.springframework.social.cafe24.config.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.social.UserIdSource;
import org.springframework.social.cafe24.api.Cafe24;
import org.springframework.social.cafe24.connect.Cafe24OAuth2Template;
import org.springframework.social.config.xml.ApiHelper;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.UsersConnectionRepository;

/**
 *
 * Interface defining the operations required for an API helper.
 * An API helper will be used to either fetch or intantiate an instance of the API binding class.
 */
public class Cafe24ApiHelper implements ApiHelper<Cafe24> {
	
	private final Logger logger = LoggerFactory.getLogger(Cafe24ApiHelper.class);
	private final UsersConnectionRepository usersConnectionRepository;

	private final UserIdSource userIdSource;

	public Cafe24ApiHelper(UsersConnectionRepository usersConnectionRepository, UserIdSource userIdSource) {
		logger.debug("Cafe24ApiHelper created");
		this.usersConnectionRepository = usersConnectionRepository;
		this.userIdSource = userIdSource;
		logger.debug("Cafe24ApiHelper userIdSource: " + userIdSource.getUserId());

	}

	@Override
	public Cafe24 getApi() {
		logger.debug("getApi() called...");
		if (logger.isDebugEnabled()) {
			logger.debug("Getting API binding instance for Cafe24");
		}
		// TODO Auto-generated method stub
		logger.debug("getApi() userIdSource.getUserId(): " + userIdSource.getUserId());

		String mallId = Cafe24OAuth2Template.getMallId();
		logger.debug("getApi mallId: " + mallId);
		Connection<Cafe24> connection = usersConnectionRepository
										.createConnectionRepository(mallId)
										.findPrimaryConnection(Cafe24.class);
		if (logger.isDebugEnabled() && connection == null) {
			logger.debug("No current connection. Returning default Cafe24Template instance.");
		}

		if (connection != null) {
			logger.debug("getApi is not null");
		} else {
			logger.debug("getApi is null");

		}
		return connection != null ? connection.getApi() : null;
	}

}
