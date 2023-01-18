package com.naturalprogrammer.springmvc.user.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.naturalprogrammer.springmvc.helpers.AbstractIntegrationTest;
import com.naturalprogrammer.springmvc.user.domain.MyUser;
import com.naturalprogrammer.springmvc.user.domain.UserId;
import com.naturalprogrammer.springmvc.user.domain.UserIdClass;
import com.naturalprogrammer.springmvc.user.dto.UserResource;
import com.naturalprogrammer.springmvc.user.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SignupIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper mapper;

    @Test
    void should_Signup() throws Exception {

        // given

        // when, then
        var result = mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                   {
                                        "email" : "user12styz@example.com",
                                        "password" : "password",
                                        "displayName" : "Sanjay567 Patel336"
                                   }       
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        var userResource = mapper.readValue(result.getResponse().getContentAsString(), UserResource.class);
        MyUser user = userRepository.findById(new UserIdClass(UserId.of(userResource.id()))).orElseThrow();
        assertThat(user.getEmail().value()).isEqualTo("user12styz@example.com");
    }
}