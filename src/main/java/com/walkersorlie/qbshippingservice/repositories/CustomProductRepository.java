package com.walkersorlie.qbshippingservice.repositories;

import com.walkersorlie.qbshippingservice.entities.Product;
import com.walkersorlie.qbshippingservice.entities.ProductCost;
import com.walkersorlie.qbshippingservice.entities.ProductOrderInformation;

import java.util.List;

public interface CustomProductRepository {

    Product findProduct(String description);

    Product updateProduct(Product product);

    Product updateProductProductCost(Product product, ProductCost productCost);

    void deleteProduct(Product product);

    void deleteProducts(List<Product> products);

    void updateProductOrderInformation(ProductOrderInformation productOrderInformation);

    void updateProductOrderInformation(List<ProductOrderInformation> productOrderInformation);

    void deleteProductOrderInformation(ProductOrderInformation productOrderInformation);
}
