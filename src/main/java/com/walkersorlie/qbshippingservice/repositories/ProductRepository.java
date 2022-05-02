package com.walkersorlie.qbshippingservice.repositories;

import com.walkersorlie.qbshippingservice.entities.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.ArrayList;
import java.util.List;

public interface ProductRepository extends MongoRepository<Product, String>, CustomProductRepository {

    List<Product> findAll();

    long count();
}
