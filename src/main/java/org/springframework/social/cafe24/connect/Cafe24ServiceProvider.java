package org.springframework.social.cafe24.connect;

import com.cafe24.devbit004.pop.social.api.Cafe24;
import com.cafe24.devbit004.pop.social.api.impl.Cafe24Template;
import com.cafe24.devbit004.pop.social.config.util.Cafe24UserIdSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.social.oauth2.AbstractOAuth2ServiceProvider;

import javax.inject.Inject;

public class Cafe24ServiceProvider extends AbstractOAuth2ServiceProvider<Cafe24> {

	// private static final String URL = ".cafe24api.com/api/v2/oauth/";
	private String mallId;

	@Inject
	private Cafe24UserIdSource cafe24UserIdSource;

	private static final Logger logger = LoggerFactory.getLogger(Cafe24ServiceProvider.class);
	static {
		logger.info("Cafe24ServiceProvider started");
	}


	public Cafe24ServiceProvider(String appId, String appSecret, String redirectUri, String scope) {
		super(new Cafe24OAuth2Template(appId, appSecret, redirectUri, scope));
	}
	
	/*private static OAuth2Template getOAuth2Template(String appId, String appSecret) {
		
		
		OAuth2Template oAuth2Template = new OAuth2Template(appId, 
				appSecret, 
				"https://utkg3000" + URL + "authorize",
				"https://utkg3000" + URL + "token");
		oAuth2Template.setUseParametersForClientAuthentication(true);
		
		return oAuth2Template;
	}*/

	@Override
	public Cafe24 getApi(String accessToken) {
		logger.info("getApi accessToken: " + accessToken);
//		String mallId = cafe24UserIdSource.getUserId();
		String tmp = Cafe24OAuth2Template.getMallId();
		this.mallId = tmp;
		logger.info("getApi tmp: " + tmp);
		logger.info("getApi mallId: " + mallId);

		// TODO Auto-generated method stub
		return new Cafe24Template(accessToken, tmp);
	}

	public String getMallId() {
		return mallId;
	}
}
