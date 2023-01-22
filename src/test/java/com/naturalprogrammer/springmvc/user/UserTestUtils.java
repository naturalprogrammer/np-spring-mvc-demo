package com.naturalprogrammer.springmvc.user;

import com.naturalprogrammer.springmvc.user.domain.MyUser;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.datafaker.Faker;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserTestUtils {

    private static final Faker FAKER = new Faker();

    public static MyUser randomUser() {
        var user = new MyUser();
        user.setId(UUID.randomUUID());
        user.setEmail(FAKER.internet().emailAddress());
        user.setPassword("{noop}Password9!");
        user.setDisplayName(FAKER.name().fullName());
        user.setLocale(Locale.forLanguageTag("en-IN"));
        user.setTokensValidFrom(Instant.now());
        return user;
    }
}
