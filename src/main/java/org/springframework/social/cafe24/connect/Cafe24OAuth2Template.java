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
import org.springframework.web.client.RestTemplate;

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



    @Override
    protected AccessGrant createAccessGrant(String accessToken,
                                            String scope,
                                            String refreshToken,
                                            Long expiresIn,
                                            Map<String, Object> response) {
        logger.info("createAccessGrant accessToken: " + accessToken);
        logger.info("createAccessGrant scope: " + scope);
        logger.info("createAccessGrant refreshToken: " + refreshToken);
        logger.info("createAccessGrant expiresIn: " + expiresIn);
        Set<String> keys = response.keySet();
        for (String key : keys) {
            logger.info("createAccessGrant response.get(" + key + "): " + response.get(key));
        }

        AccessGrant createdAccessGrant = super.createAccessGrant(accessToken, scope, refreshToken, expiresIn, response);
        logger.info("createAccessGrant createdAccessGrant.getAccessToken(): " + createdAccessGrant.getAccessToken());

        return createdAccessGrant;
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
        logger.info("exchangeForAccess 1");
        logger.info("exchangeForAccess authorizationCode: " + authorizationCode);
        logger.info("exchangeForAccess redirectUri: " + redirectUri);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
        String clientInfo = clientId + ":" + clientSecret;
        logger.info("clientId: " + clientId);
        logger.info("clientSecret: " + clientSecret);
        logger.info("clientInfo: " + clientInfo);
        /*params.set("client_id", clientId);
        params.set("client_secret", clientSecret);*/
        byte[] base64EncodedClientInfo = Base64.getEncoder().encode(clientInfo.getBytes());

        String base64EncodedStr = new String(base64EncodedClientInfo);
        logger.info("base64EncodedClientInfo base64EncodedStr: " + base64EncodedStr);

        params.set("code", authorizationCode);
        params.set("redirect_uri", redirectUri);
        params.set("grant_type", "authorization_code");
        logger.info("exchangeForAccess 2");

        if (additionalParameters != null) {
            logger.info("exchangeForAccess 3");

            params.putAll(additionalParameters);
        }
        logger.info("exchangeForAccess 4");

        /* 헤더, Access Token 발급 받기 위한 url, restTemplate 준ㅂ;*/
        HttpHeaders headers = new HttpHeaders();
        String accessTokenUrl = getAccessTokenUrl();
        RestTemplate restTemplate = getRestTemplate();

        /* 실제 헤더 값 넣기 */
        headers.set("Authorization", "Basic " + base64EncodedStr);
        // headersForAccessToken.add("Content-Type", "application/x-www-form-urlencoded");
        /* 헤더에 Content-Type 설정 */
        headers.setContentType(new MediaType("application", "x-www-form-urlencoded"));
//        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);


//        restTemplate.setDefaultUriVariables(params);

        //MultiValueMap<String, String>는 파라미터의 타입
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);
//        restTemplate.exchange(accessTokenUrl, HttpMethod.POST, entity, String.class);


        /* 실제로 AcccessTokenUrl로 직접 요청하고 응답을 받는 부분 */
        ResponseEntity<Map> responseEntity = restTemplate.exchange(accessTokenUrl, HttpMethod.POST, entity, Map.class);

   /*     logger.info("exchangeForAccess accessGrant map.get(access_token): "  + map.get("access_token"));
        logger.info("exchangeForAccess accessGrant map.get(refresh_token): "  + map.get("refresh_token"));*/
        logger.info("exchangeForAccess accessGrant map.get(refresh_token): " + responseEntity.getBody().get("access_token"));
        AccessGrant accessGrant = createAccessGrantForExchange(responseEntity.getBody());

        logger.info("exchangeForAccess accessGrant getAccessToken: "  + accessGrant.getAccessToken());
        return accessGrant;
    }

    private AccessGrant createAccessGrantForExchange(Map<String, Object> result) {
        String accessToken = (String) result.get("access_token");
        String refreshToken = (String) result.get("refresh_token");
        String  strIssuedAt = (String) result.get("issued_at");
        Long issuedAt = stringToDate(strIssuedAt);
        String  strExpiresAt = (String) result.get("expires_at");
        Long expiresAt = stringToDate(strExpiresAt);

        logger.info("createAccessGrantForExchange issuedAt: " + issuedAt);
        logger.info("createAccessGrantForExchange expiresAt: " + expiresAt);
        Long expiresIn = null;
        if (issuedAt != null && expiresAt != null) {
            expiresIn = expiresAt - issuedAt;
        }

        logger.info("createAccessGrantForExchange accessToken: " + accessToken);
        logger.info("createAccessGrantForExchange refreshToken: " + refreshToken);
        logger.info("createAccessGrantForExchange expiresIn: " + expiresIn);

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





}
