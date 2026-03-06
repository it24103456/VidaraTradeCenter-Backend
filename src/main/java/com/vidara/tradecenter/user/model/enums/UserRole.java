package com.vidara.tradecenter.user.model.enums;

public enum UserRole {

    CUSTOMER,
    ADMIN,
    AGENT;

    // HELPER METHOD
    // Returns role with "ROLE_" prefix for Spring Security
    //
    //   UserRole.ADMIN.getAuthority() → "ROLE_ADMIN"

    public String getAuthority() {
        return "ROLE_" + this.name();
    }
}