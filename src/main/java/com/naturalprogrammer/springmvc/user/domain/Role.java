package com.naturalprogrammer.springmvc.user.domain;

public enum Role {
    CUSTOMER, // Business user
    ADMIN, // Admin
    UNVERIFIED, // Email unverified
    VERIFIED; // Verified

    public String authority() {
        return "ROLE_" + name();
    }
}
