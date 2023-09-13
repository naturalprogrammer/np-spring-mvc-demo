package com.naturalprogrammer.springmvc.helpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.naturalprogrammer.springmvc.common.error.Problem;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.SoftAssertions;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.ResultMatcher;

import java.util.Collections;
import java.util.Map;

import static com.naturalprogrammer.springmvc.common.error.ProblemType.NOT_FOUND;

@Component
@RequiredArgsConstructor
public class MyResultMatchers {

    private static MyResultMatchers INSTANCE;
    private final ObjectMapper objectMapper;

    @PostConstruct
    private void setInstance() {
        INSTANCE = this;
    }

    public ResultMatcher isProblem(int expectedStatus, String type, Map<String, String> expectedErrors) {
        return mvcResult -> {

            var softly = new SoftAssertions();

            var response = mvcResult.getResponse();
            softly.assertThat(response.getStatus()).as("Status").isEqualTo(expectedStatus);
            softly.assertThat(response.getContentType()).as("Content Type").isEqualTo(MediaType.APPLICATION_PROBLEM_JSON_VALUE);

            var bodyStr = response.getContentAsString();
            var problem = objectMapper.readValue(bodyStr, Problem.class);
            softly.assertThat(problem.id()).as("Problem Id").hasSize(36);
            softly.assertThat(problem.type()).as("Problem type").isEqualTo(type);
            softly.assertThat(problem.status()).as("Status").isEqualTo(expectedStatus);
            softly.assertThat(problem.errors()).as("Error count").hasSize(expectedErrors.size());
            problem.errors().forEach(actualError -> {
                softly.assertThat(expectedErrors).as("Error code %s exists", actualError.code()).containsKey(actualError.code());
                var expectedField = expectedErrors.get(actualError.code());
                softly.assertThat(expectedField).as("Error field %d", expectedField).isEqualTo(actualError.field());
            });
            softly.assertAll();
        };
    }

    public ResultMatcher isProblem(int expectedStatus, String type, String errorCode, String errorField) {
        return isProblem(expectedStatus, type, Map.of(errorCode, errorField));
    }

    public ResultMatcher isNotFoundProblem() {
        return isProblem(404, NOT_FOUND.getType(), Collections.emptyMap());
    }

    public static MyResultMatchers result() {
        return INSTANCE;
    }
}
