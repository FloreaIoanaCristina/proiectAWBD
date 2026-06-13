package com.unibuc.management.security;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
    DOCTOR,
    PATIENT;

    @Override
    public String getAuthority() {
        return "ROLE_" + name();
    }
}
