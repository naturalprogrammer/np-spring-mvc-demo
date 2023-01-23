package com.naturalprogrammer.springmvc.user.validators;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordValidatorTest {

    private final PasswordValidator subject = new PasswordValidator();

    @ParameterizedTest
    @ValueSource(strings = {
            "Password9!",
            "0assworD@"
    })
    void testValid(String password) {
        assertThat(subject.isValid(password, null)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "password9!",
            "PASSWORD9@",
            "Pas99!@"
    })
    void testInvalid(String password) {
        assertThat(subject.isValid(password, null)).isFalse();
    }
}