package org.springframework.social.cafe24.api;

import java.util.List;
import java.util.Map;

public interface ProductOperations {

    List<Product> getProducts(Map<String, Object> params);

    Product getProduct(Long id);
}
