package org.springframework.social.cafe24.config.xml;

import com.cafe24.devbit004.pop.social.config.support.Cafe24ApiHelper;
import com.cafe24.devbit004.pop.social.connect.Cafe24ConnectionFactory;
import com.cafe24.devbit004.pop.social.security.Cafe24AuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.social.config.xml.AbstractProviderConfigBeanDefinitionParser;
import org.springframework.social.connect.ConnectionFactory;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.security.provider.SocialAuthenticationService;

import java.util.Map;
import java.util.Set;

/**
 *  스프링의 애플리케이션 컨텍스트에서 서비스 프로바이더(ex: facebook, twitter, cafe24 등)의 bean을 파싱하는 클래스.
 *  {@link AbstractProviderConfigBeanDefinitionParser}를 상속.
 *  {@link AbstractProviderConfigBeanDefinitionParser}이 하는 일:
 *  <ol>
 *      <li>{@link ConnectionFactoryLocator}이 없다면 자동적으로 등록하고
 *      {@link ConnectionFactory}를 {@link ConnectionFactoryLocator}에 등록</li>
 *  	<li>connection repository로부터 받는 request-scope의 API 바인딩 빈을 생성</li>
 *  </ol>
 */
public class Cafe24ConfigBeanDefinitionParser extends AbstractProviderConfigBeanDefinitionParser {

	private static final Logger logger = LoggerFactory.getLogger(Cafe24ConfigBeanDefinitionParser.class);

	protected Cafe24ConfigBeanDefinitionParser() {
		super(Cafe24ConnectionFactory.class, Cafe24ApiHelper.class);
	}

	@Override
	protected Class<? extends SocialAuthenticationService<?>> getAuthenticationServiceClass() {
		// TODO Auto-generated method stub
		return Cafe24AuthenticationService.class;
	}

	@Override
	protected BeanDefinition getAuthenticationServiceBeanDefinition(String appId, String appSecret,
                                                                    Map<String, Object> allAttributes) {
		logger.debug("getAuthenticationServiceBeanDefinition started...");
		Set<String> keys = allAttributes.keySet();
		for (String key : keys) {
			logger.debug("allAttributes(" + key + "): " + allAttributes.get(key));
		}

		BeanDefinitionBuilder builder = BeanDefinitionBuilder
				.genericBeanDefinition(authenticationServiceClass)
				.addConstructorArgValue(appId)
				.addConstructorArgValue(appSecret)
				.addConstructorArgValue(getRedirectUri(allAttributes))
				.addConstructorArgValue(getMallId(allAttributes));
		return builder.getBeanDefinition();
	}



	@Override
	protected BeanDefinition getConnectionFactoryBeanDefinition(String appId, String appSecret, Map<String, Object> allAttributes) {
		logger.debug("getConnectionFactoryBeanDefinition started...");
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(Cafe24ConnectionFactory.class).addConstructorArgValue(appId).addConstructorArgValue(appSecret);
		builder.addConstructorArgValue(getRedirectUri(allAttributes));
		builder.addConstructorArgValue(getMallId(allAttributes));
		return builder.getBeanDefinition();
	}

	protected String getRedirectUri(Map<String, Object> allAttributes) {
		if (allAttributes.containsKey("redirect_uri")) {
			String redirectUri = (String)allAttributes.get("redirect_uri");
			logger.debug("getRedirectUri redirectUri: " + redirectUri);

			return redirectUri.isEmpty() ? null : redirectUri;
		}

		logger.debug("getRedirectUri return null");

		return null;

	}
	protected String getMallId(Map<String, Object> allAttributes) {
		if (allAttributes.containsKey("mall_id")) {
			String mallId = (String)allAttributes.get("mall_id");
			logger.debug("getMallId mallId: " + mallId);

			return mallId.isEmpty() ? null : mallId;
		}
		logger.debug("getMallId return null");

		return null;

	}

}
