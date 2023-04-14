package com.naturalprogrammer.springmvc.user.validators;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordValidatorTest {

    private final PasswordValidator subject = new PasswordValidator();

    @ParameterizedTest
    @ValueSource(strings = {
            "Password10!",
            "0assworD@"
    })
    void testValid(String password) {
        assertThat(subject.isValid(password, null)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "password10!",
            "PASSWORD10@",
            "Pas99!@"
    })
    void testInvalid(String password) {
        assertThat(subject.isValid(password, null)).isFalse();
    }
}