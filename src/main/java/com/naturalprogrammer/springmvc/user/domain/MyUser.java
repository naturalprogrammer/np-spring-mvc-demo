package com.naturalprogrammer.springmvc.user.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

@Entity
@Table(name = "usr")
@Getter
@Setter
@ToString
@IdClass(UserIdClass.class)
public class MyUser extends AbstractEntity<UserId> {

    public static final int EMAIL_MAX = 1024;
    public static final int NAME_MIN = 1;
    public static final int NAME_MAX = 50;
    public static final int PASSWORD_MIN = 8;
    public static final int PASSWORD_MAX = 50;

    @Column(nullable = false, unique = true, length = EMAIL_MAX)
    private Email email;

    @Column(nullable = false) // no length because it will be encrypted
    @ToString.Exclude
    private Password password;

    @Column(nullable = false, length = NAME_MAX)
    private DisplayName displayName;

    @Column
    private Locale locale;

    @Enumerated(EnumType.STRING)
    private List<Role> roles = Collections.emptyList();

    @Column(length = EMAIL_MAX)
    private Email newEmail;

    // A JWT issued before this won't be valid
    @Column(nullable = false)
    private Instant tokensValidFrom;
}