package com.vidara.tradecenter.user.model.enums;

public enum UserStatus {

    ACTIVE,      // Normal user, can login and purchase
    INACTIVE,    // Account disabled by user or admin
    BANNED,      // Blocked due to violation
    PENDING      // Email not verified yet
}