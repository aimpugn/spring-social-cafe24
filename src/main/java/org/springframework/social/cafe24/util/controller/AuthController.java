package org.springframework.social.cafe24.util.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.env.Environment;
import org.springframework.social.cafe24.api.Cafe24;
import org.springframework.social.cafe24.connect.Cafe24OAuth2Connection;
import org.springframework.social.cafe24.util.UserCookieSignInAdapter;
import org.springframework.social.cafe24.util.service.ConnectService;
import org.springframework.social.connect.*;
import org.springframework.social.connect.support.OAuth2ConnectionFactory;
import org.springframework.social.connect.web.*;
import org.springframework.social.support.URIBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UrlPathHelper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.*;


@Controller("AuthController")
@RequestMapping("/auth")
public class AuthController extends ConnectController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private static final String ALGORITHM = "HmacSHA256";

    private final Environment environment;

    @Autowired
    private ConnectService connectService;

    private Cafe24 cafe24;
    private String mallId;

    private final UrlPathHelper urlPathHelper = new UrlPathHelper();

    private final MultiValueMap<Class<?>, ConnectInterceptor<?>> connectInterceptors = new LinkedMultiValueMap<Class<?>, ConnectInterceptor<?>>();
    private final ConnectionFactoryLocator connectionFactoryLocator;


    private ConnectionRepository connectionRepository;
    private UsersConnectionRepository usersConnectionRepository;


    private final ConnectSupport connectSupport;

    private SessionStrategy sessionStrategy;

    /* Sign in 과 관련된 필드 */
    private final UserCookieSignInAdapter userCookieSignInAdapter;
    private final MultiValueMap<Class<?>, ProviderSignInInterceptor<?>> signInInterceptors = new LinkedMultiValueMap<>();

    @Autowired
    public AuthController(ConnectionFactoryLocator connectionFactoryLocator, Environment environment) {
        super(connectionFactoryLocator, null);
        this.connectionFactoryLocator = connectionFactoryLocator;
        this.sessionStrategy = new HttpSessionSessionStrategy();
        this.connectSupport = new ConnectSupport(this.sessionStrategy);
        this.userCookieSignInAdapter = new UserCookieSignInAdapter();
        this.environment = environment;


    }


    @Override
    public String connectionStatus(NativeWebRequest request, Model model) {
        logger.debug("connectionStatus(NativeWebRequest request, Model model) called...");
        return "errors/error";
    }

    @Override
    @RequestMapping(value="/{providerId}", method=RequestMethod.GET)
    public String connectionStatus(@PathVariable String providerId, NativeWebRequest request, Model model) {
        logger.debug("connectionStatus(String providerId, NativeWebRequest request, Model model) called...");
        return "errors/error";
    }


    @RequestMapping(value = "/{providerId}", method = RequestMethod.GET, params = {"mall_id", "hmac"})
    public RedirectView connect(@PathVariable String providerId, @RequestParam HashMap<String , String > params, NativeWebRequest request) {
        logger.debug("service handler called...");

        if (params.size() > 0) {
            Set<String> keys = params.keySet();
            for (String key : keys) {
                logger.debug("connectionStatus params.get( " + key + " ): " + params.get(key));
            }
        }

        if (params.size() > 0) {
            /* 전달된 hmac 값을 저장 */
            String originHmac = params.get("hmac");

            /* 다른 매개변수들 통해 hmac 값 구하기 */
            Assert.notNull(params, "app 접근시 전달되는 쿼리 스트링이 null이면 안 된다.");
            String clientSecret = environment.getProperty("cafe24.app.secret");
            logger.debug("service clientSecret: " + clientSecret);
            String hmacDecodedFromQueryString = getHmac(params, clientSecret);
            logger.debug("service hmacValue: " + hmacDecodedFromQueryString);

            logger.debug("service originHmac: " + originHmac);
            if (hmacDecodedFromQueryString.equals(originHmac)) {
                logger.debug("hmac 검사 통과");

                mallId = params.get("mall_id");
                logger.debug("service after hmac test mallId: " + mallId);
                /* 원래는 SocialContext에서 @Bean으로 전달되던 ConnectionRepository를 mallId별로 만들기 위해 */
                /* hmac 검사 통과 이후 생성하도록 코드 수정 */
                connectionRepository = connectService.makeConnectionRepository(mallId);
                return super.connect(providerId, request);
            }

        }

        return new RedirectView("/errors/error", true);

    }


    @Override
    @RequestMapping(value = "/{providerId}", method = RequestMethod.GET,  params="code")
    public RedirectView oauth2Callback(@PathVariable String providerId, NativeWebRequest request) {
        Connection<?> connection = null;
        OAuth2ConnectionFactory<?> connectionFactory = null;
        RedirectView redirectView = redirect("/");
        try {
            logger.debug("oauth2Callback started...");
            logger.debug("oauth2Callback providerId: " + providerId);
            logger.debug("oauth2Callback getParameter(code): " + request.getParameter("code"));
            connectionFactory = (OAuth2ConnectionFactory<?>) connectionFactoryLocator.getConnectionFactory(providerId);
            logger.debug("oauth2Callback connectionFactory.getClass().getName(): " + connectionFactory.getClass().getName());
            String code = request.getParameter("code");
            logger.debug("oauth2Callback code: " + code);

            connection = connectSupport.completeConnection(connectionFactory, request);
            logger.debug("oauth2Callback connection done...");

//            cafe24Template = (Cafe24Template) connection.getApi();
            Cafe24OAuth2Connection cafe24OAuth2Connection = (Cafe24OAuth2Connection) connection;

            this.cafe24 =  cafe24OAuth2Connection.getApi();

            /* 연결을 JdbcConnectionRepository에 추가 */
            addConnection(connection, connectionFactory, request);


            redirectView = handleSignIn(connection, connectionFactory, request);
            logger.debug("oauth2Callback handleSignIn redirectView: " + redirectView.getUrl());

        } catch (Exception e) {
            logger.debug("e.getMessage: " + e.getMessage());

            e.getStackTrace();
            sessionStrategy.setAttribute(request, PROVIDER_ERROR_ATTRIBUTE, e);
            logger.warn("1111Exception while handling OAuth2 callback (" + e.getMessage() + "). Redirecting to " + providerId +" connection status page.");
        }
//        return connectionStatusRedirect(providerId, request);
//        return new RedirectView("/admin/login", true);
        return redirectView;
    }


    private RedirectView redirect(String url) {
        return new RedirectView(url, true);
    }


    private void addConnection(Connection<?> connection, ConnectionFactory<?> connectionFactory, WebRequest request) {
        logger.debug("addConnection started...");
        try {
            logger.debug("addConnection 1");

            connectionRepository.addConnection(connection);
            logger.debug("addConnection 2");

            //shopRepository.save();
            postConnect(connectionFactory, connection, request);
            logger.debug("addConnection 3");

        } catch (DuplicateConnectionException e) {
            logger.debug("addConnection DuplicateConnectionException");

            sessionStrategy.setAttribute(request, DUPLICATE_CONNECTION_ATTRIBUTE, e);
        }
    }

    private RedirectView handleSignIn(Connection<?> connection, ConnectionFactory<?> connectionFactory, NativeWebRequest request) {
        this.usersConnectionRepository = connectService.getUsersConnectionRepository();

        if (connection == null || connectionFactory == null) {
            return new RedirectView("/errors/error", true);
        }
        logger.debug("handleSignIn called...");
        List<String> userIds = usersConnectionRepository.findUserIdsWithConnection(connection);


        if (userIds.size() == 0) {
            ProviderSignInAttempt signInAttempt = new ProviderSignInAttempt(connection);
            logger.debug("handleSignIn userIds.size() == 0");

            sessionStrategy.setAttribute(request, ProviderSignInAttempt.SESSION_ATTRIBUTE, signInAttempt);
            return redirect("/");
        } else if (userIds.size() == 1) {
            logger.debug("handleSignIn userIds.size() == 1");

            /* 해당하는 아이디가 있다면 업데이트 */
            usersConnectionRepository.createConnectionRepository(userIds.get(0)).updateConnection(connection);

            /*  */
            String urlWhenSuccess = userCookieSignInAdapter.signIn(userIds.get(0), connection, request);
            logger.debug("handleSignIn (userIds.size() == 1) urlWhenSuccess: " + urlWhenSuccess);

            /* 유저 커넥션 업데이트 후(post)에 처리할 것 있으면 처리 */
            postSignIn(connectionFactory, connection, request);

            return urlWhenSuccess != null ? redirect(urlWhenSuccess + "?mall_id=" + cafe24.getMallId()) : redirect("/errors/error");
        } else {
            logger.debug("handleSignIn userIds.size() > 1");
            /* 해당하는 아이디가 한 개를 초과하면 멀티 유저 에러 */
            return redirect(URIBuilder.fromUri("/admin/result").queryParam("error", "multiple_users").build().toString());
        }
    }


    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void postConnect(ConnectionFactory<?> connectionFactory, Connection<?> connection, WebRequest request) {
        logger.debug("postConnect started...");
        for (ConnectInterceptor interceptor : interceptingConnectionsTo(connectionFactory)) {
            logger.debug("postConnect interceptor: " + interceptor.getClass().getName());
            interceptor.postConnect(connection, request);
        }
    }

    private void postSignIn(ConnectionFactory<?> connectionFactory, Connection<?> connection, WebRequest request) {
        for (ProviderSignInInterceptor interceptor : interceptingSignInTo(connectionFactory)) {
            logger.debug("postSignIn interceptor.getClass().getName(): " + interceptor.getClass().getName());
            interceptor.postSignIn(connection, request);

        }
    }

    /** 로그인 후 ConnectionFactory의 제네릭 타입(Cafe24)으로 저장된 인터셉터를 목록으로 반환.
     *
     * */
    private List<ProviderSignInInterceptor<?>> interceptingSignInTo(ConnectionFactory<?> connectionFactory) {
        Class<?> serviceType = GenericTypeResolver.resolveTypeArgument(connectionFactory.getClass(), ConnectionFactory.class);
        logger.debug("interceptingSignInTo serviceType.getName(): " + serviceType.getName());
        logger.debug("interceptingSignInTo serviceType.getPackage().getName(): " + serviceType.getPackage().getName());
        List<ProviderSignInInterceptor<?>> typedInterceptors = signInInterceptors.get(serviceType);
        if (typedInterceptors == null) {
            typedInterceptors = Collections.emptyList();
        }
        return typedInterceptors;
    }

    /** 연결 후 ConnectionFactory의 제네릭 타입(Cafe24)으로 저장된 인터셉터를 목록으로 반환.
     *
     * */
    private List<ConnectInterceptor<?>> interceptingConnectionsTo(ConnectionFactory<?> connectionFactory) {
        logger.debug("interceptingConnectionsTo started...");

        Class<?> serviceType = GenericTypeResolver.resolveTypeArgument(connectionFactory.getClass(), ConnectionFactory.class);
        List<ConnectInterceptor<?>> typedInterceptors = connectInterceptors.get(serviceType);
        if (typedInterceptors == null) {
            typedInterceptors = Collections.emptyList();
        }
        return typedInterceptors;
    }


    /* 연결/비연결 여부를 알려주는 페이지로 리다이렉트하기 위한 메서드 */
    protected RedirectView connectionStatusRedirect(String providerId, NativeWebRequest request) {
        logger.debug("connectionStatusRedirect started...");
        HttpServletRequest servletRequest = request.getNativeRequest(HttpServletRequest.class);
        String path = "/admin/result/";
        logger.debug("connectionStatusRedirect path: " + path);

        if (prependServletPath(servletRequest)) {
            path = servletRequest.getServletPath() + path;
            logger.debug("connectionStatusRedirect path in if(prependServletPath): " + path);

        }
        return new RedirectView(path, true);
    }

    private boolean prependServletPath(HttpServletRequest request) {
        logger.debug("prependServletPath started...");

        return !this.urlPathHelper.getPathWithinServletMapping(request).equals("");
    }



    private static String getHmac(Map<String, String> params, String clientKey) {
        /* hmac은 필요없으므로 삭제 */
        params.remove("hmac");
        /* GET으로 온 변수들을 이름순으로 정렬 */
        List<String > keys = new ArrayList<>(params.keySet());
        Collections.sort(keys);

        /* 이름순으로 정렬된 변수를 사용하여 "변수=값&변수=값&..." 형식의 쿼리 스트링 만들기 */
        StringBuilder sb = new StringBuilder();
        for (String key : keys) {
            sb.append(key).append("=").append(params.get(key)).append(!keys.get(keys.size() - 1).equals(key) ? "?" : "");

        }
        String queryString = new String(sb);
        logger.debug("getHmac queryString: " + queryString);

        String result = null;
        try {
            /* 쿼리 스트링을 URL 인코딩 하기 */
            String urlEncodedQueryString
                    = URLEncoder.encode(queryString, "UTF-8")
                    .replaceAll("(\\%3D)", "=")
                    .replaceAll("(\\%3F)", "&");

            /* key 등록 및 필요한 알고리즘 가져오기 */
            Key key = new SecretKeySpec(clientKey.getBytes("UTF-8"), ALGORITHM);
            Mac sha256 = Mac.getInstance(key.getAlgorithm());
            sha256.init(key);

            /* 쿼리 스트링 해싱하기 */
            byte[] resultByte = sha256.doFinal(urlEncodedQueryString.getBytes("UTF-8"));

            /* 해싱된 쿼리 스트링을 base64로 인코딩하기 */
            byte[] base64EncodedResultByte = Base64.getEncoder().encode(resultByte);
            result = new String(base64EncodedResultByte);
            logger.debug("getHmac result: " + result);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException | InvalidKeyException e) {
            e.printStackTrace();
        }
        return result;
    }
}
