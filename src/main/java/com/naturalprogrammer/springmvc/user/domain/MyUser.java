package com.naturalprogrammer.springmvc.user.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;

@Entity
@Table(name = "usr")
@Getter
@Setter
@IdClass(UserId.class)
public class MyUser extends AbstractEntity<UserId> {

    public static final int EMAIL_MAX = 1024;
    public static final int NAME_MIN = 1;
    public static final int NAME_MAX = 50;

    @Column(nullable = false, unique = true, length = EMAIL_MAX)
    private Email email;

    @Column(nullable = false) // no length because it will be encrypted
    private Password password;

    @Column(nullable = false, length = NAME_MAX)
    private DisplayName displayName;

    @Column
    private Locale locale;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "usr_role", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private Set<Role> roles = Collections.emptySet();

    @Column(length = EMAIL_MAX)
    private Email newEmail;

    // A JWT issued before this won't be valid
    @Column(nullable = false)
    private Instant tokensValidFrom;
}