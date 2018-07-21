package org.springframework.social.cafe24.connect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.social.cafe24.api.Cafe24;
import org.springframework.social.connect.ApiAdapter;
import org.springframework.social.connect.ConnectionValues;
import org.springframework.social.connect.UserProfile;

public class Cafe24Adapter implements ApiAdapter<Cafe24> {
	private static final Logger logger = LoggerFactory.getLogger(Cafe24Adapter.class);
	@Override
	public boolean test(Cafe24 api) {
		// TODO Auto-generated method stub
		logger.info("return true");
		return true;
	}

	@Override
	public void setConnectionValues(Cafe24 cafe24, ConnectionValues values) {
		// TODO Auto-generated method stub

		logger.info("setConnectionValues called...");
		String mallId = cafe24.getMallId();
		values.setProviderUserId(mallId);
	}

	/*  UserProfile(String id, String name, String firstName, String lastName, String email, String username) { */
	@Override
	public UserProfile fetchUserProfile(Cafe24 api) {
		// TODO Auto-generated method stub
		logger.info("fetchUserProfile return null...");
		UserProfile userProfile = new UserProfile(api.getMallId(),
				api.getMallId(),
				null,
				null,
				null,
				null);
		return userProfile;
	}

	@Override
	public void updateStatus(Cafe24 api, String message) {
		// TODO Auto-generated method stub
		logger.info("updateStatus called...");


	}

}
