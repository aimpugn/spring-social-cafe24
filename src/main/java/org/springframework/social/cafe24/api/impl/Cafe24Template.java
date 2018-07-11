package org.springframework.social.cafe24.api.impl;

import com.alibaba.fastjson.JSON;
import com.cafe24.devbit004.pop.social.api.Cafe24;
import com.cafe24.devbit004.pop.social.api.Product;
import com.cafe24.devbit004.pop.social.api.ProductOperations;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.social.oauth2.AbstractOAuth2ApiBinding;
import org.springframework.social.oauth2.TokenStrategy;
import org.springframework.social.support.ClientHttpRequestFactorySelector;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.*;

/**
 * {@link AbstractOAuth2ApiBinding}을 상속하고 {@link Cafe24}를 구현.<br>
 * Controller에서 사용할 API의 인스턴스
 *
 */
public class Cafe24Template extends AbstractOAuth2ApiBinding implements Cafe24 {

	private static final Logger logger = LoggerFactory.getLogger(Cafe24Template.class);


	private final String mallId;

	private ObjectMapper objectMapper;

	private ProductOperations productOperations;

	static {
		logger.debug("Cafe24Template called...");
	}

	public Cafe24Template(String accessToken, String mallId) {
		/* AUTHORIZATION_HEADER를 사용하면 Authorization 헤더에 액세스 토큰 함께 전달. */
		/* OAuth2.0 버전이 Bearer로 "Authorization: Bearer {accessToken}"으로 전달된다 */
		super(accessToken, TokenStrategy.AUTHORIZATION_HEADER);
		this.mallId = mallId;
		logger.debug("mallId: " + this.mallId);
		initialize();
	}

	/**<ol>
	 * <li>ClientHttpRequestFactorySelector.bufferRequests(getRestTemplate().getRequestFactory())를 RequestFactory에 설정하여
	 * response를 반복하여 읽을 수 있다. </li>
	 * <li>initSubApi() 실행.</li>
	 * </ol>
	 */
	private void initialize() {
		logger.debug("initialize started...");
		super.setRequestFactory(ClientHttpRequestFactorySelector.bufferRequests(getRestTemplate().getRequestFactory()));
		initSubApi();
	}

	/**
	 * {@link com.cafe24.devbit004.pop.social.api.Cafe24Api}에 정의된 operations들을 구현한
	 * 각 template의 인스턴스를 생성.
	 */
	private void initSubApi() {
		logger.debug("initSubApi started...");
		productOperations = new ProductTemplate(this);
	}


	/**
	 * @return api를 호출할 mallId에 해당하는 Url 생성
	 */
	private String getBaseApiUrl() {
		logger.debug("getBaseApiUrl: " + "https://" + this.mallId + ".cafe24api.com/api/v2/admin");
		return "https://" + this.mallId + ".cafe24api.com/api/v2/admin";
	}



	/* 단일 객체 가져오기 */
	@Override
	public <T> T fetchObject(Class<T> type, HttpMethod httpMethod, Map<String, Object> query, String data, Object... endPoints) {
		URI uri = makeUri(getBaseApiUrl(), query, endPoints);
		HttpHeaders headers = new HttpHeaders();

		/* 한글이 섞여있기 때문에 application/json;charset=UTF-8로 Content-Type 설정 */
		HttpEntity<String> httpEntity;
		if (data == null) {
			httpEntity = new HttpEntity<>(null, headers);
		} else {
			httpEntity = new HttpEntity<>(data, headers);
		}

		headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
		ResponseEntity<JsonNode> responseEntity
				/* exchange의 매개변수는 (uri, 통신 방식, HttpEntity, 반환 받을 타입) */
				= getRestTemplate().exchange(uri, httpMethod, httpEntity, JsonNode.class);
		JsonNode jsonNode = responseEntity.getBody();
		String key = getKeyForBody(endPoints);

		String str = jsonNode.textValue();
		logger.debug("fetchObject str: " + str);
		return deserializeObject(jsonNode.get(key), type);
	}

	@Override
	public <T> List<T> fetchObjects(Class<T> type, HttpMethod httpMethod, String data, Object... endPoints) {
		return fetchObjects(type, httpMethod, null, data, endPoints);
	}


	/**
	 *
	 *
	 * @param type products, orders 등 api를 호출할 곳 지정
	 * @param httpMethod api 호출 결과를 반환 받을 타입 지정. (ex: {@link Product})
	 * @param query 엔드포인트 뒤에 붙일 쿼리 스트링
	 * @param data api 호출할 때 함께 전달할 쿼리 스트링을
	 * @param endPoints API 호출하기 위한 url 경로 완성용 엔드포인트 배열
	 * @return API 호출한 결과 type에 해당하는 리스트 반환
	 */
//	public <T> List<T> fetchObjects(String connectionType, Class<T> type, Map<String, Object> params) {
	public <T> List<T> fetchObjects(Class<T> type, HttpMethod httpMethod, Map<String, Object> query, String data, Object... endPoints) {
		logger.debug("fetchObjects called...");

		/* API 호출 위한 URI 만들기 */
		URI uri = makeUri(getBaseApiUrl(), query, endPoints);

		logger.debug("fetchObjects uri.toString(): "  + uri.toString());
		logger.debug("fetchObjects uri.getPath(): "  + uri.getPath());
		logger.debug("fetchObjects uri.getHost(): "  + uri.getHost());
		logger.debug("fetchObjects uri.getScheme(): "  + uri.getScheme());
		logger.debug("fetchObjects uri.getUserInfo(): "  + uri.getUserInfo());
		logger.debug("fetchObjects uri.getAuthority(): "  + uri.getAuthority());
		logger.debug("fetchObjects uri.getFragment(): "  + uri.getFragment());

		/* restTemplate으로 통신할 때 사용할 헤더 설정 */
		HttpHeaders headers = new HttpHeaders();

		/* 한글이 섞여있기 때문에 application/json;charset=UTF-8로 Content-Type 설정 */
		headers.setContentType(MediaType.APPLICATION_JSON_UTF8);

		/* MultiValueMap. 제네릭은 body의 타입으로 지정한다. header는 HttpHeaders 내부에서 MultiValueMap으로 구현되어 있다. */
		HttpEntity<String> httpEntity;
		if (data == null) {
			httpEntity = new HttpEntity<>(null, headers);
		} else {
			httpEntity = new HttpEntity<>(data, headers);
		}


		/* RestTemplate을 쓰면 URLEncoder.encode(itemIds, "UTF-8");이 된다 */
		/* API를 실제로 요청하는 부분. ResponseEntity<반환 받을 타입>으로 지정 */
		ResponseEntity<JsonNode> responseEntity
				/* exchange의 매개변수는 (uri, 통신 방식, HttpEntity, 반환 받을 타입) */
				= getRestTemplate().exchange(uri, httpMethod, httpEntity, JsonNode.class);
		logger.debug("fetchObjects responseEntity getStatusCode: "  + responseEntity.getStatusCode());
		logger.debug("fetchObjects responseEntity getStatusCodeValue: "  + responseEntity.getStatusCodeValue());
		logger.debug("fetchObjects responseEntity getHeaders().getLocation(): "  + responseEntity.getHeaders().getLocation());

		/* restTemplate에서 통신한 결과 받은 responseEntity에서 필요한 body를 꺼낸다. */
		JsonNode jsonNode = responseEntity.getBody();

		/* endPoints에 따른 key를 찾아서 해당 key로 body를 꺼낸다 */
		String key = getKeyForBody(endPoints);
		logger.debug("fetchObjects key: " + key);
		/* 전달 받은 JsonNode 객체에서 products, orders 등 원하는 값을 받아서 역직렬화하여 리스트로 만들어 반환 */
		return deserializeDataList(jsonNode.get(key), type);
	}


	/**
	 * API 호출하기 위한 URI 생성 메서드
	 * @param baseUrl 몰별로 생성되는 baseUrl
	 * @param query 쿼리를 함께 전달해야 하는 경우 Map<String, Object> 타입으로 넘긴다.
	 * @param endPoints /products, /orders, /products/118/variants 등과 같이 admin 후에 붙일 엔드 포인트를 전달
	 * @return "baseUrl/엔드포인트?쿼리" 형식으로 URI를 생성하여 반환
	 */
	private static URI makeUri(String baseUrl, Map<String, Object> query, Object... endPoints) {
		logger.debug("makreUrl called... ");
		try {
			/* StringBuilder와 달리 StringBuffer는 스레드 세이프 */
			StringBuffer sb = new StringBuffer();

			/* endPoints 붙이기*/
			if (endPoints.length > 0) {
				for (Object endPoint : endPoints) {
					sb.append("/").append(endPoint);
				}
			}
			logger.debug("makreUrl after endpoints part ended: " + sb.toString());

			String queryDelimiter = "?";
			if(URI.create(baseUrl).getQuery() != null) {
				queryDelimiter = "&";
			}

			/* 쿼리 스트링 붙이기 */
			String key;
			Object value;
			if (query != null && query.size() > 0) {
				sb.append(queryDelimiter);
				Set<String> keys = query.keySet();
				for (Iterator<String> iterator = keys.iterator(); iterator.hasNext();) {
					key = iterator.next();
					value = query.get(key);
					sb.append(key).append("=");
					if (value != null) {
						sb.append(formEncode(value.toString()));
					}
					if (iterator.hasNext()) {
						sb.append("&");
					}
				}
			}
			logger.debug("makreUrl after query part ended: " + sb.toString());

			/* 기존 URL에 이미 쿼리 스트링이 있다면 "&"을 붙이고, 없다면 "?"을 붙인다. */

			return new URI( baseUrl + ( sb.length() > 0 ? sb.toString() : ""));
		} catch (URISyntaxException e) {
			throw new RuntimeException("URISyntaxException occurred...");
		}

	}

	private static String formEncode(String data) {
		try {
			return URLEncoder.encode(data, "UTF-8");
		}
		catch (UnsupportedEncodingException wontHappen) {
			throw new IllegalStateException(wontHappen);
		}
	}


	/**
	 * fetchObjects로 JsonNode 결과를 받아올 때 body를 꺼내기 위한 key를 endPoints로 얻어내는 메서드
	 * @param endPoints API 엔드 포인트 배열
	 * @return 엔드 포인트 배열로부터 JsonNode에서 body를 꺼낼 key
	 */
	private String getKeyForBody(Object... endPoints) {
		logger.debug("getKeyForBody called...");
		String key = null;
		List<Object> keys;
		if (endPoints.length > 0) {
			keys = Arrays.asList(endPoints);
			if (keys.contains("count")) {
				key = "count";
			} else {
				if (keys.contains("inventories")) {
					key = "inventory";
				} else if (keys.contains("salesvolume")) {
					key = "salesvolume";
				} else {
					if (endPoints.length % 2 != 0) {
						key = endPoints[endPoints.length - 1].toString();
					} else {
						/* varints -> variant, prducts -> product */
						key = endPoints[endPoints.length - 2].toString();
						key = key.substring(0, key.length() - 1);
					}
				}
			}
		}
		return key;
	}

	/**
	 * JsonNode를 역직렬화하여 List<T>로 반환하려는 메서드
	 * @param jsonNode Api 서버와 통신 결과 반환 받은 JsonNode 객체
	 * @param classType Product.class 등 반환 받으려는 객체의 타입
	 */

	@SuppressWarnings("unchecked")
	private <T> List<T> deserializeDataList(JsonNode jsonNode, final Class<T> classType) {
		logger.debug("deserializeDataList called...");


		logger.debug("jsonNode.toString(): " + jsonNode.toString());
		logger.debug("deserializeDataList try to make CollectionType listType");

		String jsonString = jsonNode.toString();

		logger.debug("deserializeDataList List<T> result = JSON.parseArray(jsonString, classType)");

		return JSON.parseArray(jsonString, classType);
	}

	private <T> T deserializeObject(JsonNode jsonNode, final Class<T> classType) {
		logger.debug("jsonNode.toString(): " + jsonNode.toString());
		String jsonString = jsonNode.toString();
		T result = JSON.parseObject(jsonString, classType);
		return result;
	}


	/**
	 * {@link AbstractOAuth2ApiBinding}가 생성되면서 함께 생성되는 RestTemplate 인스턴스 사용.
	 * @return restTemplate
	 */
	@Override
	public RestTemplate getRestTemplate() {
		logger.debug("getRestTemplate called...");

		return super.getRestTemplate();
	}

	/**
	 * 특정 서비스 프로바이더를 위한 설정을 할 수 있는 메서드. <br>
	 * 메시지 컨버터, 에러 핸들러 등을 등록할 수 있다.
	 * @param restTemplate 데코레이션할 restTemplate
	 */
	@Override
	protected void configureRestTemplate(RestTemplate restTemplate) {
		logger.debug("configureRestTemplate called...");
		super.configureRestTemplate(restTemplate);
	}

	/**
	 * {@link AbstractOAuth2ApiBinding}에서 생성된 restTemplate에 대한 설정 메서드
	 * @param restTemplate 통신에 사용할 RestTemplate
	 * @return restTemplate
	 */
	@Override
	protected RestTemplate postProcess(RestTemplate restTemplate) {
		logger.debug("postProcess called");
		return super.postProcess(restTemplate);
	}


	/**
	 * {@link AbstractOAuth2ApiBinding}에서 HttpMessageConverter 타입의 다양한 메시지 컨버터를 등록할 때 사용할
	 * JsonMessageConverter를 전달한다. Target 클래스와 Mixin 클래스가 설정된 Cafe24Module을 등록한다.
	 * @return Cafe24Moudle이 등록된 ObjectMapper를 {@link MappingJackson2HttpMessageConverter}에 설정하여 반환
	 */
	/*@Override
	protected MappingJackson2HttpMessageConverter getJsonMessageConverter() {
		logger.debug("getJsonMessageConverter called...");

		MappingJackson2HttpMessageConverter converter = super.getJsonMessageConverter();
		objectMapper = new ObjectMapper();
		objectMapper.registerModule(new Cafe24Module());
		logger.debug("getJsonMessageConverter Cafe24Module registered...");

		converter.setObjectMapper(objectMapper);
		return converter;
	}*/


	/**
	 * 현재 인증을 받은 mall의 id를 반환한다.
	 * @return mallId
	 */
	public String getMallId() {
		return mallId;
	}

	/**
	 * {@link ProductOperations}에 미리 정의된 메서드들을 사용할 수 있다.
	 * @return initSubApi에서 생성한 productTemplate를 {@link ProductOperations} 인터페이스 타입으로 반환.
	 */
	@Override
	public ProductOperations productOperations() {
		logger.debug("productOperations called...");
		return productOperations;
	}


}
