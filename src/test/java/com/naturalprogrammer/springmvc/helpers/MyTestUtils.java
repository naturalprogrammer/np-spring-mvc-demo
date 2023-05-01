package com.naturalprogrammer.springmvc.helpers;

import com.naturalprogrammer.springmvc.common.MessageGetter;
import com.naturalprogrammer.springmvc.common.error.Problem;
import com.naturalprogrammer.springmvc.common.error.ProblemBuilder;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.ObjectFactory;

import java.util.*;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class MyTestUtils {

    public static final Validator TEST_VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    public static void mockValidator(Validator validator) {
        given(validator.validate(any())).willAnswer(invocation ->
                TEST_VALIDATOR.validate(invocation.getArgument(0))
        );
    }

    public static void mockProblemBuilder(ObjectFactory<ProblemBuilder> problemComposer) {
        var messageGetter = mock(MessageGetter.class);
        given(messageGetter.getMessage(any(), any())).willAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            return Arrays.stream(args).map(Object::toString).collect(Collectors.joining());
        });
        given(problemComposer.getObject()).willReturn(new ProblemBuilder(messageGetter));
    }

    public static Problem randomProblem() {
        return new Problem(
                UUID.randomUUID().toString(),
                "/problems/invalid-signup",
                "Invalid fields received while doing signup",
                422,
                "SignupRequest{email='null', displayName='null'}",
                null,
                Collections.emptyList()
        );
    }

    public static Date futureTime() {
        return DateUtils.truncate(DateUtils.addHours(new Date(), 1), Calendar.SECOND);
    }

    public static Date pastTime() {
        return DateUtils.truncate(DateUtils.addHours(new Date(), -1), Calendar.SECOND);
    }

}
