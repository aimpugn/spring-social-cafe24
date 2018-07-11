package org.springframework.social.cafe24.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public class Product implements Serializable {
    private static final Logger logger = LoggerFactory.getLogger(Product.class);

    private final Long shopNo;

    private final Long productNo;

    private final  String productCode;

    private final String productName;

    private final String detailImage;
    static {
        logger.debug("Product.class static block");
    }


    public Product(Long shopNo, Long productNo, String productCode, String productName, String detailImage) {
        logger.debug("api.Product class constructor called");

        this.shopNo = shopNo;
        this.productNo = productNo;
        this.productCode = productCode;
        this.productName = productName;
        this.detailImage = detailImage;
        logger.debug("api.Product instance constructed");

    }

    public Long getShopNo() {
        return shopNo;
    }

    public Long getProductNo() {
        return productNo;
    }

    public String getProductCode() {
        return productCode;
    }

    public String getProductName() {
        return productName;
    }

    public String getDetailImage() {
        return detailImage;
    }

    @Override
    public String toString() {
        return "Product{" +
                "shopNo=" + shopNo +
                ", productNo='" + productNo + '\'' +
                ", productCode='" + productCode + '\'' +
                ", productName='" + productName + '\'' +
                ", detailImage=" + detailImage +
                '}';
    }
}
