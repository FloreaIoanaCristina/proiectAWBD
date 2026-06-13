package com.unibuc.management.security;

import org.springframework.security.core.GrantedAuthority;

import java.io.Serializable;
import java.util.Collection;
import org.springframework.security.core.userdetails.User;

public class CustomUserDetails extends User implements Serializable {
    private static final long serialVersionUID = 1L;
    private final Long id;

    public CustomUserDetails(Long id, String username, String password, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}