package com.walkersorlie.qbshippingservice;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest
@DataMongoTest
@ExtendWith(SpringExtension.class)
public class ApplicationUITests {

}
