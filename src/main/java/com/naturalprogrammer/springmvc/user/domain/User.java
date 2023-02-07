package com.naturalprogrammer.springmvc.user.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Entity
@Table(name = "usr")
@Getter
@Setter
@ToString(callSuper = true)
public class User extends AbstractEntity {

    public static final int EMAIL_MAX = 1024;
    public static final int NAME_MIN = 1;
    public static final int NAME_MAX = 50;
    public static final int PASSWORD_MIN = 8;
    public static final int PASSWORD_MAX = 50;

    @Column(nullable = false, unique = true, length = EMAIL_MAX)
    private String email;

    @Column(nullable = false) // no length because it will be encrypted
    @ToString.Exclude
    private String password;

    @Column(nullable = false, length = NAME_MAX)
    private String displayName;

    @Column
    private Locale locale;

    @Enumerated(EnumType.STRING)
    private Set<Role> roles = new HashSet<>();

    @Column(length = EMAIL_MAX)
    private String newEmail;

    // A JWT issued before this won't be valid
    @Column(nullable = false)
    private Instant tokensValidFrom;

    public boolean hasRoles(Role ...roles) {
        return this.roles.containsAll(List.of(roles));
    }
}