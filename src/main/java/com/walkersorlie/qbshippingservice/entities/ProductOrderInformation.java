package com.walkersorlie.qbshippingservice.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("product_order_information")
public class ProductOrderInformation {
    @Id
    private String id;
    private Double totalFreightCost;
    private Double totalFreightWeight;
    private int quantity;
    private int purchaseOrder;


    public ProductOrderInformation(String id, Double totalFreightCost, Double totalFreightWeight, int quantity, int purchaseOrder) {
        this.id = id;
        this.totalFreightCost = totalFreightCost;
        this.totalFreightWeight = totalFreightWeight;
        this.quantity = quantity;
        this.purchaseOrder = purchaseOrder;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Double getTotalFreightCost() {
        return totalFreightCost;
    }

    public void setTotalFreightCost(Double totalFreightCost) {
        this.totalFreightCost = totalFreightCost;
    }

    public Double getTotalFreightWeight() {
        return totalFreightWeight;
    }

    public void setTotalFreightWeight(Double totalFreightWeight) {
        this.totalFreightWeight = totalFreightWeight;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getPurchaseOrder() {
        return this.purchaseOrder;
    }

    public void setPurcaseOrder(int purchaseOrder) {
        this.purchaseOrder = purchaseOrder;
    }
}
