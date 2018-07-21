package org.springframework.social.cafe24.connect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.social.oauth2.AccessGrant;
import org.springframework.social.oauth2.GrantType;
import org.springframework.social.oauth2.OAuth2Parameters;
import org.springframework.social.oauth2.OAuth2Template;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Cafe24OAuth2Template extends OAuth2Template {
    private static final Logger logger = LoggerFactory.getLogger(Cafe24OAuth2Template.class);

    private final String redirectUri;
    private final String clientInfo;
    private final String scope;
    private final String clientId;
    private final String clientSecret;
    private String authorizationHeader;


    private static String mallId;

    public Cafe24OAuth2Template(String appId, String appSecret, String redirectUri, String scope) {
        super(appId, appSecret, getAuthorizeUrl(), getAccessTokenUrl());
        logger.info("Cafe24OAuth2Template appId: " + appId);
        logger.info("Cafe24OAuth2Template appSecret: " + appSecret);
        this.redirectUri = redirectUri;
        this.clientInfo = "client_id=" + formEncode(appId);
        this.scope = scope;
        this.clientId = appId;
        this.clientSecret = appSecret;
        this.authorizationHeader = "Basic " + new String(Base64.getEncoder().encode((clientId + ":" + clientSecret).getBytes()));
    }

    protected static String getAuthorizeUrl() {
        String authorizeUrl = "https://" + mallId + ".cafe24api.com/api/v2/oauth/authorize";
        logger.info("getAuthorizeUrl authorizeUrl: " + authorizeUrl);
        return authorizeUrl;
    }

    protected static String getAccessTokenUrl() {
        String accessTokenUrl = "https://" + mallId + ".cafe24api.com/api/v2/oauth/token";
        logger.info("getAccessTokenUrl accessTokenUrl: " + accessTokenUrl);
        return accessTokenUrl;
    }





    /* code를 얻기 위한 Url을 생성하는 메서드(그랜트 타입을 주지 않는 경우) */
    @Override
    public String buildAuthorizeUrl(OAuth2Parameters parameters) {
        logger.info("buildAuthorizeUrl1 started ");
        return buildAuthorizeUrl(GrantType.AUTHORIZATION_CODE, parameters);
    }

    /* code를 얻기 위한 Url을 생성하는 메서드(그랜트 타입을 주는 경우) */
    @Override
    public String buildAuthorizeUrl(GrantType grantType, OAuth2Parameters parameters) {
        logger.info("buildAuthorizeUrl2 started ");
        logger.debug("parameters.get(is_multi_shop): " + parameters.get("is_multi_shop"));
        logger.debug("parameters.get(lang): " + parameters.get("lang"));
        logger.debug("parameters.get(shop_no): " + parameters.get("shop_no"));
        logger.debug("parameters.get(timestamp): " + parameters.get("timestamp"));
        logger.debug("parameters.get(user_id): " + parameters.get("user_id"));
        logger.debug("parameters.get(user_name): " + parameters.get("user_name"));
        logger.debug("parameters.get(user_type): " + parameters.get("user_type"));
        logger.debug("parameters.get(hmac): " + parameters.get("hmac"));
        parameters.remove("is_multi_shop");
        parameters.remove("lang");
        parameters.remove("shop_no");
        parameters.remove("timestamp");
        parameters.remove("user_id");
        parameters.remove("user_name");
        parameters.remove("user_type");
        parameters.remove("hmac");

        Set<String> keys = parameters.keySet();
        for (String key : keys) {
            logger.info("key: " + key);
        }
        for (String key : keys) {
            logger.info("buildAuthorizeUrl parameter.get(" + key + "): " + parameters.get(key));
        }
        if (parameters.get("mall_id") != null) {
            this.mallId = String.valueOf(parameters.get("mall_id"));
        }
        logger.info("buildAuthorizeUrl redirectUri" + redirectUri);
//        logger.info("buildAuthorizeUrl getAuthorizeUrl: " + getAuthorizeUrl(String.valueOf(parameters.get("mallId"))));
        logger.info("buildAuthorizeUrl super.buildAuthorizeUrl(grantType, parameters): " + super.buildAuthorizeUrl(grantType, parameters));

        if (redirectUri != null) parameters.setRedirectUri(redirectUri);
        String authorizeUrl = customBuildAuthUrl(grantType, parameters);
        logger.info("buildAuthorizeUrl authorizeUrl: " + authorizeUrl);
        return authorizeUrl;
    }


    @Override
    public AccessGrant exchangeForAccess(String authorizationCode,
                                         String redirectUri,
                                         MultiValueMap<String, String> additionalParameters) {
        logger.info("exchangeForAccess authorizationCode: " + authorizationCode);
        logger.info("exchangeForAccess redirectUri: " + redirectUri);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();

        logger.info("base64EncodedClientInfo base64EncodedStr: " + authorizationHeader);

        params.set("code", authorizationCode);
        params.set("redirect_uri", redirectUri);
        params.set("grant_type", "authorization_code");

        if (additionalParameters != null) {
            logger.info("exchangeForAccess additionalParameters == null");
            params.putAll(additionalParameters);
        }

        AccessGrant accessGrant = postForAccessGrant(params);

        logger.info("exchangeForAccess accessGrant getAccessToken: "  + accessGrant.getAccessToken());
        logger.info("exchangeForAccess accessGrant getRefreshToken: "  + accessGrant.getRefreshToken());
        logger.info("exchangeForAccess accessGrant getExpireTime: "  + accessGrant.getExpireTime());
        logger.info("exchangeForAccess accessGrant getScope: "  + accessGrant.getScope());


        return accessGrant;
    }


    @Override
    public AccessGrant refreshAccess(String refreshToken, MultiValueMap<String, String> additionalParameters) {
        String mallIdToRefresh = additionalParameters.get("mallId").get(0);

        logger.debug("refreshAccess mallIdToRefresh: " + mallIdToRefresh);
        setMallId(mallIdToRefresh);
        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
        params.set("refresh_token", refreshToken);
        params.set("grant_type", "refresh_token");

        AccessGrant accessGrant = postForAccessGrant(params);

        logger.info("refreshAccess accessGrant getAccessToken: "  + accessGrant.getAccessToken());
        logger.info("refreshAccess accessGrant getRefreshToken: "  + accessGrant.getRefreshToken());
        logger.info("refreshAccess accessGrant getExpireTime: "  + accessGrant.getExpireTime());
        logger.info("refreshAccess accessGrant getScope: "  + accessGrant.getScope());
        return accessGrant;
    }

    private AccessGrant postForAccessGrant(MultiValueMap<String, String> params) {
        /* 헤더, Access Token 발급 받기 위한 url, restTemplate 준비*/
        HttpHeaders headers = new HttpHeaders();

        /* 실제 헤더 값 넣기 */
        headers.set("Authorization", authorizationHeader);
        logger.debug("postForAccessGrant authorizationHeader: " + authorizationHeader);

        /* 헤더에 Content-Type 설정 */

        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);  // == headers.setContentType(new MediaType("application", "x-www-form-urlencoded"));
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);

        /* 실제로 AcccessTokenUrl로 직접 요청하고 응답을 받는 부분 */
        ResponseEntity<Map> responseEntity = getRestTemplate().exchange(getAccessTokenUrl(), HttpMethod.POST, entity, Map.class);
        Map<String, Object> result = responseEntity.getBody();

        /* 결과 확인 로그 */
        for (String key : result.keySet()) {
            logger.debug("postForAccessGrant result.get(" + key + "): " + result.get(key));
        }
        return createAccessGrant(result);
    }

    private AccessGrant createAccessGrant(Map result) {
        String accessToken = (String) result.get("access_token");
        String refreshToken = (String) result.get("refresh_token");
        String  strIssuedAt = (String) result.get("issued_at");
        Long issuedAt = stringToDate(strIssuedAt);
        String  strExpiresAt = (String) result.get("expires_at");
        Long expiresAt = stringToDate(strExpiresAt);

        logger.info("createAccessGrant issuedAt: " + issuedAt);
        logger.info("createAccessGrant expiresAt: " + expiresAt);
        Long expiresIn = null;
        if (issuedAt != null && expiresAt != null) {
            expiresIn = expiresAt - issuedAt;
            logger.info("createAccessGrant expiresIn: " + expiresIn);
            expiresIn /= 1000l;

        }
        logger.info("createAccessGrant expiresIn/1000l: " + expiresIn);

        logger.info("createAccessGrant accessToken: " + accessToken);
        logger.info("createAccessGrant refreshToken: " + refreshToken);

        return new AccessGrant(accessToken, scope, refreshToken, expiresIn);
    }


    private String customBuildAuthUrl(GrantType grantType, OAuth2Parameters parameters) {
        mallId = parameters.get("mall_id").get(0);
        String baseAuthUrl = "https://"
                + mallId
                + ".cafe24api.com/api/v2/oauth/authorize?" + clientInfo;
        parameters.remove("mall_id");
        StringBuilder authUrl = new StringBuilder(baseAuthUrl);
        if (grantType == GrantType.AUTHORIZATION_CODE) {
            authUrl.append('&').append("response_type").append('=').append("code");
        } else if (grantType == GrantType.IMPLICIT_GRANT) {
            authUrl.append('&').append("response_type").append('=').append("token");
        }
        for (Iterator<Map.Entry<String, List<String>>> additionalParams = parameters.entrySet().iterator(); additionalParams.hasNext();) {
            Map.Entry<String, List<String>> param = additionalParams.next();
            String name = formEncode(param.getKey());
            logger.info("name: " + name);
            for (Iterator<String> values = param.getValue().iterator(); values.hasNext();) {
                authUrl.append('&').append(name).append('=').append(formEncode(values.next()));
            }
        }
        authUrl.append("&").append("scope").append("=").append(scope);
        return authUrl.toString();
    }

    private String formEncode(String data) {
        try {
            return URLEncoder.encode(data, "UTF-8");
        }
        catch (UnsupportedEncodingException ex) {
            // should not happen, UTF-8 is always supported
            throw new IllegalStateException(ex);
        }
    }

    private static Long stringToDate(String str){
        Long result = null;
        SimpleDateFormat sdf = new SimpleDateFormat();
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S").parse(str);
            logger.info("stringToDate date: " + date.toString());
            result = date.getTime();
            logger.info("stringToDate result: " + result);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return result;
    }


    public static String getMallId() {
        return mallId;
    }

    public static void setMallId(String mallId) {
        Cafe24OAuth2Template.mallId = mallId;
    }
}
