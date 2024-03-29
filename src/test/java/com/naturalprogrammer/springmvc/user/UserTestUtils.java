package com.naturalprogrammer.springmvc.user;

import com.naturalprogrammer.springmvc.user.domain.User;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.datafaker.Faker;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserTestUtils {

    public static final Faker FAKER = new Faker();
    public static final String RANDOM_USER_PASSWORD = "Password9!";

    public static User randomUser() {
        var user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(FAKER.internet().emailAddress());
        user.setPassword("{noop}" + RANDOM_USER_PASSWORD);
        user.setDisplayName(FAKER.name().fullName());
        user.setLocale(Locale.forLanguageTag("en-IN"));
        user.setTokensValidFrom(Instant.now().truncatedTo(ChronoUnit.SECONDS));
        return user;
    }
}
