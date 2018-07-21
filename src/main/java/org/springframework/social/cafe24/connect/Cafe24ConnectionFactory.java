package org.springframework.social.cafe24.connect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.social.ServiceProvider;
import org.springframework.social.cafe24.api.Cafe24;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionData;
import org.springframework.social.connect.support.OAuth2ConnectionFactory;
import org.springframework.social.oauth2.AccessGrant;
import org.springframework.social.oauth2.OAuth2ServiceProvider;

public class Cafe24ConnectionFactory extends OAuth2ConnectionFactory<Cafe24> {

	private static final Logger logger = LoggerFactory.getLogger(Cafe24ConnectionFactory.class);
	private ServiceProvider<Cafe24> serviceProvider;
	static {
		logger.info("Cafe24ConnectionFactory started");
	}
	public Cafe24ConnectionFactory(String appId, String appSecret, String redirectUri, String scope) {
//		this(appId, appSecret, null, null);
		super("cafe24", new Cafe24ServiceProvider(appId, appSecret, redirectUri, scope), new Cafe24Adapter());
		this.serviceProvider = super.getServiceProvider();
	}



	/**
	 * {@link org.springframework.social.connect.web.ConnectSupport}의 completeConnection 메서드를 통해 accessGrant가 전달된다.
	 * 전달된 acceessGrant를 사용해서 {@link Cafe24OAuth2Connection}를 생성해서 반환한다.
	 * @param accessGrant
	 * @return Cafe24OAuth2Connection 생성하여 반환
	 */
	@Override
	public Connection<Cafe24> createConnection(AccessGrant accessGrant) {
		logger.info("Override createConnection(AccessGrant accessGrant) started...");

		/* OAuth 인증 받을 때 저장되는 mallId를 가져와서 connection을 생성 */
		String mallId = Cafe24OAuth2Template.getMallId();
		logger.info("Override createConnection getMallId: " + mallId);
		String accessToken = accessGrant.getAccessToken();
		logger.info("Override createConnection accessToken: " + accessToken);
		Long expireTime = accessGrant.getExpireTime();
		logger.info("Override createConnection expireTime: " + expireTime);
		/* Cafe24ServiceProvider - getApi accessToken이 호출된다 */
		/* OAuth2Connection을 생성하면서 getApi(accessToken)을 통해 Cafe24Template을 생성 */
		logger.info("Override createConnection makes OAuth2Connection<>( cafe24, "
				+ mallId + ", "
				+ accessToken + ", "
				+ accessGrant.getRefreshToken() + ", "
				+ expireTime + ", "
				+ serviceProvider.getClass().getName() + ", "
				+ getApiAdapter().getClass().getName() + " )");

		/* providerUserId에 mallId가 들어가도록 연결 생성 */
		return new Cafe24OAuth2Connection("cafe24", mallId, accessToken,
				accessGrant.getRefreshToken(), expireTime, (OAuth2ServiceProvider) serviceProvider, getApiAdapter());
	}

	/**
	 * JdbcConnectionRepository에서 findConnections를 통해 {@link Connection}을 찾을 때,
	 * JdbcConnectionRepository의 이너 클래스인 {@link org.springframework.social.connect.jdbc.JdbcConnectionRepository.ServiceProviderConnectionMapper}가 objectMapper라는 이름으로 {@link org.springframework.jdbc.core.JdbcTemplate}의 query로 전달되고,
	 * JdbcTemplate의 query 메서드에서 {@link org.springframework.jdbc.core.RowMapperResultSetExtractor}의 extractData 메서드가 호출하면,
	 * objectMapper의 Connection<?> mapRow(ResultSet rs, int rowNum) 메서드와 ConnectionData mapConnectionData(ResultSet rs)가 호출된다.
	 * 이때 UserConnection 테이블에서 가져온 기존 연결 데이터를 가지고 {@link Cafe24ConnectionFactory}의 createConnection(connectionData)가 호출한다.
	 *
     *
	 * @param data
	 * @return 전달된 연결 데이터에 해당하는 새로운 {@link Cafe24OAuth2Connection}을 생성하여 반환
     * @author Kim Sang Hyun
	 */
	@Override
	public Connection<Cafe24> createConnection(ConnectionData data) {
		logger.info("Override createConnection(ConnectionData data) started...");

		return new Cafe24OAuth2Connection(data, (OAuth2ServiceProvider<Cafe24>) serviceProvider, getApiAdapter());
	}
}
