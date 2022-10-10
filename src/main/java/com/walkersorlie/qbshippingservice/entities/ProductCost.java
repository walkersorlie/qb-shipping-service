package com.walkersorlie.qbshippingservice.entities;

import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductCost that = (ProductCost) o;
        return cost.equals(that.cost) && productId.equals(that.productId) && dateCreated.equals(that.dateCreated);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cost, productId, dateCreated);
    }
}
