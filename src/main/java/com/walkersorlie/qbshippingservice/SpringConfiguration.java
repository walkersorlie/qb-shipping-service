package com.walkersorlie.qbshippingservice;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.walkersorlie.qbshippingservice.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;

//@Configuration
//@ComponentScan
//public class SpringConfiguration {


//
//    @Value("${spring.data.mongodb.uri}")
//    private String connectionString;
//    @Value("${spring.data.mongodb.database}")
//    private String database;
//
//    @Bean
//    public MongoClient mongoClient() {
//        return MongoClients.create(new ConnectionString(connectionString));
//    }
//
//    @Bean
//    public MongoTemplate mongoTemplate() {
//        return new MongoTemplate(mongoClient(), database);
//    }
//
//    @Override
//    protected String getDatabaseName() {
//        return database;
//    }
//}
