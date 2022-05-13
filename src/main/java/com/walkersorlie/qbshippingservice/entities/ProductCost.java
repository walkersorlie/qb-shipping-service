package com.walkersorlie.qbshippingservice.entities;

import org.springframework.data.mongodb.core.mapping.Field;

public class ProductCost {
    private Double cost;

    @Field("product_id")
    private String productId;

    @Field("date_created")
    private String dateCreated;

    public ProductCost(String dateCreated, Double cost, String productId) {
        this.dateCreated = dateCreated;
        this.cost = cost;
        this.productId = productId;
    }

    public Double getCost() {
        return cost;
    }

    public void setCost(Double cost) {
        this.cost = cost;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getDateCreated() {
        return dateCreated;
    }
}
