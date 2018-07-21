package org.springframework.social.cafe24.connect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.GenericTypeResolver;
import org.springframework.social.ExpiredAuthorizationException;
import org.springframework.social.ServiceProvider;
import org.springframework.social.cafe24.api.Cafe24;
import org.springframework.social.connect.ApiAdapter;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionData;
import org.springframework.social.connect.support.AbstractConnection;
import org.springframework.social.oauth2.AccessGrant;
import org.springframework.social.oauth2.OAuth2ServiceProvider;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author aimpugn
 */
public class Cafe24OAuth2Connection extends AbstractConnection<Cafe24> {

    private static final Logger logger = LoggerFactory.getLogger(Cafe24OAuth2Connection.class);
    private static final long serialVersionUID = 4057584084077577480L;

    private transient final OAuth2ServiceProvider<Cafe24> serviceProvider;

    private String accessToken;

    private String refreshToken;

    private Long expireTime;

    private transient Cafe24 api;

    private transient Cafe24 apiProxy;

    private String mallId;

    private static MultiValueMap<String, String> additionalParameters = new LinkedMultiValueMap<>();

    /**
     * ConnectionData를 전달 받아서 {@link Cafe24OAuth2Connection} 생성.
     * {@link ConnectionData}로부터 존재하는 {@link Connection}을 재구성할 때 호출되도록 설계.
     * @param data 해당 연결의 상태를 가지는 데이터
     * @param serviceProvider OAuth2 기반의 서비스 프로바이더
     * @param apiAdapter 서비스 프로바이더를 위한 ApiAdapter
     */
    public Cafe24OAuth2Connection(ConnectionData data, OAuth2ServiceProvider<Cafe24> serviceProvider, ApiAdapter<Cafe24> apiAdapter) {
        super(data, apiAdapter);
        logger.debug("Cafe24OAuth2Connection(ConnectionData data, OAuth2ServiceProvider<Cafe24> serviceProvider, ApiAdapter<Cafe24> apiAdapter) called...");
        this.serviceProvider = serviceProvider;
        this.mallId = data.getProviderUserId();
        initAccessTokens(data.getAccessToken(), data.getRefreshToken(), data.getExpireTime());
        logger.debug("data.getAccessToken(): " + data.getAccessToken() + ", "
                + "data.getRefreshToken(): " + data.getRefreshToken(), ", "
                + "data.getExpireTime(): " + data.getExpireTime());
        initApi();
        /* DB에서 가져온 연결이 만료되지 않았다면 apiProxy를 활성화한다. */
        if (!hasExpired()) {
            initApiProxy();
        }
    }

    public Cafe24OAuth2Connection(String providerId, String providerUserId, String accessToken, String refreshToken, Long expireTime, OAuth2ServiceProvider serviceProvider, ApiAdapter<Cafe24> apiAdapter) {
        super(apiAdapter);
        logger.debug("Cafe24OAuth2Connection(String providerId, String providerUserId, String accessToken, String refreshToken, Long expireTime, OAuth2ServiceProvider serviceProvider, ApiAdapter<Cafe24> apiAdapter) called...");
        this.serviceProvider = serviceProvider;
        this.mallId = providerUserId;
        initAccessTokens(accessToken, refreshToken, expireTime);
        initApi();
        initApiProxy();
        initKey(providerId, providerUserId);
        logger.info("Cafe24OAuth2Connection created...");
    }


    private void initAccessTokens(String accessToken, String refreshToken, Long expireTime) {
        logger.info("initAccessTokens called...");

        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expireTime = expireTime;
        logger.debug("initAccessTokens accessToken: " + accessToken);
        logger.debug("initAccessTokens refreshToken: " + refreshToken);
        logger.debug("initAccessTokens expireTime: " + expireTime);

    }

    /**
     * 서비스 프로바이더에 액세스 토큰을 전달하여 Cafe24Template 객체를 반환받아서 필드에 저장한다
     */
    private void initApi() {
        logger.info("initApi called...");
        api = serviceProvider.getApi(accessToken);
        logger.debug("initApi api.getMallId(): " + api.getMallId());

    }


    /**
     * getApi()를 할 때마다 호출되는 메서드.
     * @return apiProxy가 있다면 apiProxy를, 없다면 api 자체를 반환.
     */
    @Override
    public Cafe24 getApi() {
        logger.info("getApi called...");
        /* 언제 apiProxy가 null인가?? */
        if (apiProxy != null) {
            logger.info("getApi when apiProxy is not null...");

            return apiProxy;
        } else {
            logger.info("getApi when apiProxy is null...");

            synchronized (getMonitor()) {
                return api;
            }
        }
    }

    /**
     * 현재 Cafe24OAuth2Connection 클래스에 저장된 정보를 ConnectionData 타입으로 만들어서 반환
     * @return 새로운 ConnectionData 생성하여 반환
     */
    @Override
    public ConnectionData createData() {
        logger.info("createData called...");

        synchronized (getMonitor()) {
            ConnectionData connectionData
                    = new ConnectionData(getKey().getProviderId(), getKey().getProviderUserId(), getDisplayName(), getProfileUrl(), getImageUrl(), accessToken, null, refreshToken, expireTime);
            logger.info("connectionData getProviderUserId: " + connectionData.getProviderUserId());
            logger.info("connectionData getAccessToken: " + connectionData.getAccessToken());
            logger.info("connectionData getProviderId: " + connectionData.getProviderId());
            logger.info("connectionData getRefreshToken: " + connectionData.getRefreshToken());
            logger.info("connectionData getDisplayName: " + connectionData.getDisplayName());

            return connectionData;
        }
    }

    /* refresh는 현재 api에 대한 refresh */

    /**
     *  {@link org.springframework.social.oauth2.OAuth2Operations}를 구현하는 {@link Cafe24OAuth2Template}에서 구현한
     *  refreshAccess 메서드를 호출한다.
     */
    @Override
    public void refresh() {
        synchronized (getMonitor()) {

            /* set을 하면 매번 새로운 List를 생성하여 담는다. */
            additionalParameters.set("mallId", this.mallId);

            logger.debug("refresh called...");
            AccessGrant refreshedAccessGrant = null;
            refreshedAccessGrant = serviceProvider.getOAuthOperations().refreshAccess(refreshToken, additionalParameters);

            if (refreshedAccessGrant != null) {
                logger.debug("refresh accessGrant.getAccessToken(): "  + refreshedAccessGrant.getAccessToken());
                logger.debug("refresh accessGrant.getRefreshToken(): "  + refreshedAccessGrant.getRefreshToken());
                logger.debug("refresh accessGrant.getExpireTime(): "  + refreshedAccessGrant.getExpireTime());


                logger.debug("refresh initAccessTokens");
                initAccessTokens(refreshedAccessGrant.getAccessToken(), refreshedAccessGrant.getRefreshToken(), refreshedAccessGrant.getExpireTime());
                logger.debug("refresh initApi");
                initApi();
                logger.debug("refresh initApiProxy");
                initApiProxy();
                logger.debug("refresh done...");
            }


        }
    }

    /**<p>
     * 필드에 저장된 expireTime을 System.currentTimeMillis로 비교한다. <br>
     * 현재 (expireTime - 600,000)을 하여 만료 시간 10분 전에 갱신되도록 설정.
     * </p>
     * @return true: 만료 전 10분 미만  <br> false: 만료 전 10분 이상
     */
    public boolean hasExpired() {
        synchronized (getMonitor()) {
            logger.debug("hasExpired? expireTime: " + expireTime);
            logger.debug("hasExpired? expireTime != null && System.currentTimeMillis() >= expireTime: " + (expireTime != null && System.currentTimeMillis() >= expireTime));
            return expireTime != null && System.currentTimeMillis() >= (expireTime - 600000);
        }
    }

    /* proxy는 끼워넣기를 할 때 주로 사용한다. 원본이 있으면 그 대신 대행을 하고, 원본에 새로운 데이터를 주입하는 등의 작업을 할 떄 사용. */
    /* 웹 클라이언트... 통신 대행. 웹 서버 대신 캐시... 토큰 받아오는 객체. */
    /* 인터페이스가 있을 때 그 인터페이스의 대행 프록시를 만들 때 Proxy.newProxyInstance 사용 */

    /**
     *
     * 이미 존재하는 클래스의 기능을 수정하거나 추가할 때 프록시 객체를 만들어 사용한다.
     *
     */
    @SuppressWarnings("unchecked")
    private void initApiProxy() {

        logger.info("initApiProxy called...");

        /* 서비스 제공자 클래스에 설정된 특정 api 타입을 가져온다 */
        Class<?> apiType = GenericTypeResolver.resolveTypeArgument(serviceProvider.getClass(), ServiceProvider.class);
        logger.info("initApiProxy apiType.getName(): " + apiType.getName()); // com.cafe24.devbit004.pop.social.api.Cafe24

        /* Cafe24의 타입이 인터페이스면 동적으로 apiProxy를 만든다. */
        if (apiType.isInterface()) {
            logger.info("initApiProxy apiType is Interface: ");

            apiProxy = (Cafe24) Proxy.newProxyInstance(apiType.getClassLoader(), new Class<?>[]{apiType}, new ApiInvocationHandler());
            logger.info("initApiProxy apiProxy.getMallId(): " + apiProxy.getMallId());

        }
    }

    private class ApiInvocationHandler implements InvocationHandler {

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            logger.info("ApiInvocationHandler invoke called... ");

            logger.debug("proxy.getClass().getName(): " + proxy.getClass().getName());
            logger.debug("method.getName(): " + method.getName());
            logger.debug("method.getDeclaringClass(): " + method.getDeclaringClass());

            synchronized (getMonitor()) {
                if (hasExpired()) {
                    logger.info("ApiInvocationHandler hasExpired() throw new ExpiredAuthorizationException(getKey().getProviderId())");
                    throw new ExpiredAuthorizationException(getKey().getProviderId());
//                    refresh();
                }
                try {
                    logger.info("ApiInvocationHandler not hasExpired() method.invoke(Cafe24OAuth2Connection.this.api, args)");

                    return method.invoke(Cafe24OAuth2Connection.this.api, args);
                } catch (InvocationTargetException e) {
                    throw e.getTargetException();
                }
            }
        }
    }


    // equas() and hashCode() generated by Eclipse
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((accessToken == null) ? 0 : accessToken.hashCode());
        result = prime * result + ((expireTime == null) ? 0 : expireTime.hashCode());
        result = prime * result + ((refreshToken == null) ? 0 : refreshToken.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!super.equals(obj)) return false;
        if (getClass() != obj.getClass()) return false;
        @SuppressWarnings("rawtypes")
        Cafe24OAuth2Connection other = (Cafe24OAuth2Connection) obj;

        if (accessToken == null) {
            if (other.accessToken != null) return false;
        } else if (!accessToken.equals(other.accessToken)) return false;

        if (expireTime == null) {
            if (other.expireTime != null) return false;
        } else if (!expireTime.equals(other.expireTime)) return false;

        if (refreshToken == null) {
            if (other.refreshToken != null) return false;
        } else if (!refreshToken.equals(other.refreshToken)) return false;

        return true;
    }


}
