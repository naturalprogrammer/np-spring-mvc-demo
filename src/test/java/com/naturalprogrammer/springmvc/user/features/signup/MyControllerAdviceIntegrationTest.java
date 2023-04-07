package com.naturalprogrammer.springmvc.user.features.signup;

import com.naturalprogrammer.springmvc.helpers.AbstractIntegrationTest;
import com.naturalprogrammer.springmvc.user.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import static com.naturalprogrammer.springmvc.common.Path.USERS;
import static com.naturalprogrammer.springmvc.common.error.ProblemType.*;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MyControllerAdviceIntegrationTest extends AbstractIntegrationTest {

    @MockBean
    private UserRepository userRepository;

    @Test
    void should_handleHttpMediaTypeNotSupportedException() throws Exception {

        // when, then
        mvc.perform(post(USERS)
                        .contentType(MediaType.APPLICATION_JSON) // Unsupported media type
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("id").isString())
                .andExpect(jsonPath("type").value(HTTP_MEDIA_TYPE_NOT_SUPPORTED.getType()))
                .andExpect(jsonPath("title").value("Http media type not supported. Maybe you aren't using the VND type?"))
                .andExpect(jsonPath("detail").value("Content-Type 'application/json' is not supported"))
                .andExpect(jsonPath("status").value("400"))
                .andExpect(jsonPath("errors", hasSize(0)));
    }


    @Test
    void should_handleHttpMessageNotReadableException() throws Exception {

        // given
        given(userRepository.existsByEmail(any())).willThrow(new RuntimeException("Foo Unhandled exception"));

        // when, then
        mvc.perform(post(USERS)
                        .contentType(SignupRequest.CONTENT_TYPE)
                        .content("{ email ")) // malformed JSON
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("id").isString())
                .andExpect(jsonPath("type").value(HTTP_MESSAGE_NOT_READABLE.getType()))
                .andExpect(jsonPath("title").value("Http message not readable. Maybe malformed JSON?"))
                .andExpect(jsonPath("detail").value("JSON parse error: Unexpected character ('e' (code 101)): was expecting double-quote to start field name"))
                .andExpect(jsonPath("status").value("400"))
                .andExpect(jsonPath("errors", hasSize(0)));
    }

    @Test
    void should_handleGenericException() throws Exception {

        // given
        given(userRepository.existsByEmail(any())).willThrow(new RuntimeException("Foo Unhandled exception"));

        // when, then
        mvc.perform(post(USERS)
                        .contentType(SignupRequest.CONTENT_TYPE)
                        .content("""
                                   {
                                        "email" : "email245585@example.com",
                                        "password" : "Password9!",
                                        "displayName" : "Sanjay457 Patel983"
                                   }
                                """))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("id").isString())
                .andExpect(jsonPath("type").value(GENERIC_ERROR.getType()))
                .andExpect(jsonPath("title").value("Generic error"))
                .andExpect(jsonPath("detail").doesNotExist())
                .andExpect(jsonPath("status").value("500"))
                .andExpect(jsonPath("errors", hasSize(0)));
    }

}