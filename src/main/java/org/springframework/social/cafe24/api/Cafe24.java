package org.springframework.social.cafe24.api;

import org.springframework.social.ApiBinding;

/**
 *  {@link Cafe24Api}와 {@link ApiBinding}을 상속하는 클래스.</br>
 *  {@link org.springframework.social.cafe24.api.impl.Cafe24Template}사용할 operations들을 정의해둔다.</br>
 *
 *
 */
public interface Cafe24 extends Cafe24Api, ApiBinding {


    String getMallId();

    ProductOperations productOperations();
}
