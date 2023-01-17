package com.naturalprogrammer.springmvc.helpers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@Sql("classpath:/test-data/sql/before-each-test.sql")
public abstract class AbstractIntegrationTest {

    @Autowired
    protected MockMvc mvc;
}
