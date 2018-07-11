package org.springframework.social.cafe24.connect;

import com.cafe24.devbit004.pop.social.api.Cafe24;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.social.ServiceProvider;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionData;
import org.springframework.social.connect.support.OAuth2ConnectionFactory;
import org.springframework.social.oauth2.AccessGrant;
import org.springframework.social.oauth2.OAuth2ServiceProvider;

public class Cafe24ConnectionFactory extends OAuth2ConnectionFactory<Cafe24> {

	private static final Logger logger = LoggerFactory.getLogger(Cafe24ConnectionFactory.class);
	private final ServiceProvider<Cafe24> serviceProvider;
	static {
		logger.info("Cafe24ConnectionFactory started");
	}
	public Cafe24ConnectionFactory(String appId, String appSecret, String redirectUri, String scope) {
//		this(appId, appSecret, null, null);
		super("cafe24", new Cafe24ServiceProvider(appId, appSecret, redirectUri, scope), new Cafe24Adapter());
		this.serviceProvider = super.getServiceProvider();
	}

	@Override
	public Connection<Cafe24> createConnection(AccessGrant accessGrant) {
		logger.info("Override createConnection(AccessGrant accessGrant) started...");

		String mallId = Cafe24OAuth2Template.getMallId();
		logger.info("Override createConnection getMallId: " + mallId);
		String accessToken = accessGrant.getAccessToken();
		logger.info("Override createConnection accessToken: " + accessToken);
		Long expireTime = accessGrant.getExpireTime();
		logger.info("Override createConnection expireTime: " + expireTime);
		/* Cafe24ServiceProvider - getApi accessToken이 호출된다 */
		OAuth2ServiceProvider<Cafe24> oAuth2ServiceProvider = (OAuth2ServiceProvider<Cafe24>) getServiceProvider();
		/* OAuth2Connection을 생성하면서 getApi(accessToken)을 통해 Cafe24Template을 생성 */
		logger.info("Override createConnection makes OAuth2Connection<>( cafe24, "
				+ mallId + ", "
				+ accessToken + ", "
				+ accessGrant.getRefreshToken() + ", "
				+ expireTime + ", "
				+ oAuth2ServiceProvider.getClass().getName() + ", "
				+ getApiAdapter().getClass().getName());

//		return new OAuth2Connection<>("cafe24", mallId, accessToken,
//				accessGrant.getRefreshToken(), expireTime, oAuth2ServiceProvider, getApiAdapter());
		return new Cafe24OAuth2Connection("cafe24", mallId, accessToken,
				accessGrant.getRefreshToken(), expireTime, oAuth2ServiceProvider, getApiAdapter());
	}

	@Override
	public Connection<Cafe24> createConnection(ConnectionData data) {
		logger.info("Override createConnection(ConnectionData data) started...");

		return super.createConnection(data);
	}
}
