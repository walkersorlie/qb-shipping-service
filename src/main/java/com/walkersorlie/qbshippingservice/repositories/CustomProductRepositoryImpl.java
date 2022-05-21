package com.walkersorlie.qbshippingservice.repositories;

import com.mongodb.client.result.UpdateResult;
import com.walkersorlie.qbshippingservice.entities.Product;
import com.walkersorlie.qbshippingservice.entities.ProductCost;
import com.walkersorlie.qbshippingservice.entities.ProductOrderInformation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.core.query.UpdateDefinition;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CustomProductRepositoryImpl implements CustomProductRepository {

    @Autowired
    MongoTemplate mongoTemplate;

    /**
     *
     * @param descrpition: the description of the Product to find
     * @return Product matching the description
     */
    @Override
    public Product findProductByDescription(String descrpition) {
        Query query = new Query(Criteria.where("description").is(descrpition));
        return mongoTemplate.findOne(query, Product.class);
    }

    /**
     *
     * @param product: Product to update
     * Updates all attributes of Product that can be changed:
     *               cost
     *               weight
     *               productCost
     * @return updated Product
     */
    @Override
    public Product updateProduct(Product product) {
        Query query = new Query(Criteria.where("id").is(product.getId()));
        UpdateDefinition update = new Update().
                set("cost", product.getCost()).
                set("weight", product.getWeight()).
                set("product_cost", product.getProductCosts());

        return mongoTemplate.update(Product.class)
                .matching(query)
                .apply(update)
                .withOptions(FindAndModifyOptions.options().returnNew(true))
                .findAndModifyValue();
    }

    /**
     *
     * @param product: Product to update
     * @param productCost: ProductCost to add to Product
     *                   Can only update "product_cost"
     * @return updated Product
     */
    @Override
    public Product updateProductProductCost(Product product, ProductCost productCost) {
        product.addProductCost(productCost);

        // this creates a new array of "product_cost" each time
        // maybe add productCost to array if not already there, or update if is already present?
        Query query = new Query(Criteria.where("id").is(product.getId()));
        UpdateDefinition update = new Update().set("product_cost", product.getProductCosts());

        return mongoTemplate.update(Product.class)
                .matching(query)
                .apply(update)
                .withOptions(FindAndModifyOptions.options().returnNew(true))
                .findAndModifyValue();
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
