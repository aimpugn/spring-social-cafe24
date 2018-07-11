package org.springframework.social.cafe24.api;

import java.io.Serializable;

public class Category implements Serializable {
    private Long categoryNo;

    public Long getCategoryNo() {
        return categoryNo;
    }

    public void setCategoryNo(Long categoryNo) {
        this.categoryNo = categoryNo;
    }

    @Override
    public String toString() {
        return "Category{" +
                "categoryNo=" + categoryNo +
                '}';
    }
}
