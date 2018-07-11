package org.springframework.social.cafe24.api;

import com.cafe24.devbit004.pop.social.api.impl.Cafe24Template;
import org.springframework.http.HttpMethod;

import java.util.List;
import java.util.Map;

/**
 * {@link Cafe24}를 구현하는 {@link Cafe24Template}에서 사용할
 * 범용 API 호출 메서드를 정의하는 인터페이스
 */
interface Cafe24Api {


    <T> List<T> fetchObjects(Class<T> type, HttpMethod httpMethod, Map<String, Object> query, String data, Object... endPoints);
    <T> List<T> fetchObjects(Class<T> type, HttpMethod httpMethod, String data, Object... endPoints);

    <T> T fetchObject(Class<T> type, HttpMethod httpMethod, Map<String, Object> query, String data, Object... endPoints);
}
