package com.naturalprogrammer.springmvc.user.controllers;

import com.naturalprogrammer.springmvc.helpers.AbstractIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SignupIntegrationTest extends AbstractIntegrationTest {

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void should_Signup() throws Exception {

        // given

        // when, then
        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                   {
                                        "email" : "user12styz@example.com",
                                        "password" : "password",
                                        "displayName" : "Sanjay567 Patel336"
                                   }       
                                """))
                .andExpect(status().isCreated());
    }
}