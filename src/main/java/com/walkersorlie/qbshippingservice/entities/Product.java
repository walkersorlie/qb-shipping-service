package com.walkersorlie.qbshippingservice.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;

@Document("products")
public class Product {
    @Id
    private String id;
    private String name;
    private String description;
    private Double cost;
    private Double weight;
    private ArrayList<String> orderInformationIds;

    public Product(String id, String name, String description, Double cost, Double weight) {
        super();
        this.id = id;
        this.name = name;
        this.description = description;
        this.cost = cost;
        this.weight = weight;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getCost() {
        return cost;
    }

    public void setCost(Double cost) {
        this.cost = cost;
    }

    public Double getWeight() {
        return weight == null ? 0 : weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public ArrayList<String> getOrderInformationIds() {
        return orderInformationIds;
    }

    public void setOrderInformationIds(ArrayList<String> orderInformationIds) {
        this.orderInformationIds = orderInformationIds;
    }

    public void addOrderInformation(ArrayList<String> orderInformation) {
        this.orderInformationIds.addAll(orderInformation);
    }

    public void addOrderInformation(String orderInformationId) {
        this.orderInformationIds.add(orderInformationId);
    }

}
