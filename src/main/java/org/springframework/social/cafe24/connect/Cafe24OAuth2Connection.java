package org.springframework.social.cafe24.connect;

import com.cafe24.devbit004.pop.social.api.Cafe24;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.GenericTypeResolver;
import org.springframework.social.ExpiredAuthorizationException;
import org.springframework.social.ServiceProvider;
import org.springframework.social.connect.ApiAdapter;
import org.springframework.social.connect.ConnectionData;
import org.springframework.social.connect.support.AbstractConnection;
import org.springframework.social.oauth2.AccessGrant;
import org.springframework.social.oauth2.OAuth2ServiceProvider;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class Cafe24OAuth2Connection extends AbstractConnection<Cafe24> {
    private static final Logger logger = LoggerFactory.getLogger(Cafe24OAuth2Connection.class);
    private static final long serialVersionUID = 4057584084077577480L;

    private transient final OAuth2ServiceProvider<Cafe24> serviceProvider;

    private String accessToken;

    private String refreshToken;

    private Long expireTime;

    private transient Cafe24 api;

    private transient Cafe24 apiProxy;


    public Cafe24OAuth2Connection(String providerId, String providerUserId, String accessToken, String refreshToken, Long expireTime, OAuth2ServiceProvider serviceProvider, ApiAdapter<Cafe24> apiAdapter) {
        super(apiAdapter);
        this.serviceProvider = serviceProvider;
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
    }

    private void initApi() {
        logger.info("initApi called...");
        /* 서비스 프로바이더에 액세스 토큰을 전달하여 Cafe24Template 객체를 반환 */
        api = serviceProvider.getApi(accessToken);
    }

    @SuppressWarnings("unchecked")
    private void initApiProxy() {
        logger.info("initApiProxy called...");

        Class<?> apiType = GenericTypeResolver.resolveTypeArgument(serviceProvider.getClass(), ServiceProvider.class);
        logger.info("initApiProxy apiType.getName(): " + apiType.getName());

        if (apiType.isInterface()) {
            logger.info("initApiProxy apiType is Interface: ");

            apiProxy = (Cafe24) Proxy.newProxyInstance(apiType.getClassLoader(), new Class<?>[] { apiType }, new ApiInvocationHandler());
            logger.info("initApiProxy apiProxy.getMallId(): " + apiProxy.getMallId());

        }
    }

    @Override
    public Cafe24 getApi() {
        logger.info("getApi called...");

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

    @Override
    public void refresh() {
        synchronized (getMonitor()) {
            AccessGrant accessGrant = serviceProvider.getOAuthOperations().refreshAccess(refreshToken, null);
            initAccessTokens(accessGrant.getAccessToken(), accessGrant.getRefreshToken(), accessGrant.getExpireTime());
            initApi();
        }
    }

    // implementing Connection

    public boolean hasExpired() {
        synchronized (getMonitor()) {
            return expireTime != null && System.currentTimeMillis() >= expireTime;
        }
    }

    private class ApiInvocationHandler implements InvocationHandler {

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            logger.info("ApiInvocationHandler invoke called... ");

            synchronized (getMonitor()) {
                if (hasExpired()) {
                    logger.info("ApiInvocationHandler hasExpired() throw new ExpiredAuthorizationException(getKey().getProviderId())");

                    throw new ExpiredAuthorizationException(getKey().getProviderId());
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
