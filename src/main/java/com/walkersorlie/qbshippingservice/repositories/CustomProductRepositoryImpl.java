package com.walkersorlie.qbshippingservice.repositories;

import com.walkersorlie.qbshippingservice.entities.Product;
import com.walkersorlie.qbshippingservice.entities.ProductOrderInformation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CustomProductRepositoryImpl implements CustomProductRepository {

    @Autowired
    MongoTemplate mongoTemplate;

    @Override
    public Product findProduct(String descrpition) {
        return null;
    }

    @Override
    public Product updateProduct(Product product) {
//        Query query = new Query(Criteria.where("id").is(product.getId()));
//        Update update = new Update();
//
//       UpdateResult result = mongoTemplate.updateFirst(query, update, Product.class);
       return product;
    }

    @Override
    public void deleteProduct(Product product) {

    }

    @Override
    public void deleteProducts(List<Product> products) {

    }

    @Override
    public void updateProductOrderInformation(ProductOrderInformation productOrderInformation) {

    }

    @Override
    public void updateProductOrderInformation(List<ProductOrderInformation> productOrderInformation) {

    }

    @Override
    public void deleteProductOrderInformation(ProductOrderInformation productOrderInformation) {

    }
}
