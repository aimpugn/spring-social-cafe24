package org.springframework.social.cafe24.connect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.social.cafe24.api.Cafe24;
import org.springframework.social.cafe24.api.impl.Cafe24Template;
import org.springframework.social.oauth2.AbstractOAuth2ServiceProvider;


public class Cafe24ServiceProvider extends AbstractOAuth2ServiceProvider<Cafe24> {
	private static final Logger logger = LoggerFactory.getLogger(Cafe24ServiceProvider.class);
	private String mallId;


	static {
		logger.info("Cafe24ServiceProvider started");
	}


	public Cafe24ServiceProvider(String appId, String appSecret, String redirectUri, String scope) {
		super(new Cafe24OAuth2Template(appId, appSecret, redirectUri, scope));
	}


	@Override
	public Cafe24 getApi(String accessToken) {
		logger.info("getApi accessToken: " + accessToken);
		String tmp = Cafe24OAuth2Template.getMallId();
		this.mallId = tmp;
		logger.info("getApi tmp: " + tmp);
		logger.info("getApi mallId: " + mallId);
		return new Cafe24Template(accessToken, tmp);
	}

	public String getMallId() {
		return mallId;
	}
}
