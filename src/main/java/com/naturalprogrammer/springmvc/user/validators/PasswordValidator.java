package com.naturalprogrammer.springmvc.user.validators;

import com.naturalprogrammer.springmvc.user.domain.MyUser;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    // At least 1 upper, lower, special characters and digit, min 8 chars, max 50 chars
    public static final String PASSWORD_REGEX = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&-+=()!])(?=\\S+$).{"
            + MyUser.PASSWORD_MIN + "," + MyUser.PASSWORD_MAX + "}$";
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(PASSWORD_REGEX);

    @Override
    public boolean isValid(String password, ConstraintValidatorContext constraintValidatorContext) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }
}
