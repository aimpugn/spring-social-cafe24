package org.springframework.social.cafe24.api.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.social.cafe24.api.Cafe24;
import org.springframework.social.cafe24.api.Product;
import org.springframework.social.cafe24.api.ProductOperations;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductTemplate implements ProductOperations {
    private static final Logger logger = LoggerFactory.getLogger(ProductTemplate.class);

    private Cafe24 cafe24;

    public ProductTemplate(Cafe24 cafe24) {
        this.cafe24 = cafe24;
    }


    /**
     * 상품 리스트 가져오기. 매개변수 전달해서 limit 조절 가능. limit는 최대 100.
     * @param query MultivalueMap
     * @return Product 타입의 리스트 반환
     */
    @Override
    public List<Product> getProducts(Map<String, Object> query) {
        logger.debug("getProducts(Map<String, Object> params) called...");
        if (query == null) {
            query = new HashMap<>();
        }

        logger.debug("getProducts(Map<String, Object> params) params.get(limit): " + query.get("limit"));
        logger.debug("getProducts cafe24.fetchObjects(products, Product.class, " + query.get("limit") + ")...");

        List<Product> products = cafe24.fetchObjects(Product.class, HttpMethod.GET, query, null, "products");
        if (products == null) {
            logger.debug("getProducts products empty...");
        }
        return products;
    }

    @Override
    public Product getProduct(Long productNo) {
        logger.debug("getProduct called...");
        Product product = cafe24.fetchObject(Product.class, HttpMethod.GET, null,null, "products", productNo);
        logger.debug("product: " + product);
        return product;
    }
}
