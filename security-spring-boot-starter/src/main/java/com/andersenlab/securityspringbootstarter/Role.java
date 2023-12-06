package com.andersenlab.securityspringbootstarter;

import org.springframework.security.core.GrantedAuthority;

import java.util.Set;

import static com.andersenlab.securityspringbootstarter.Authority.*;

public enum Role {

    RECEPTIONIST(Set.of(APARTMENTS_READ, CLIENTS_CREATE, CHECK_IN, CHECK_OUT, CALCULATE_PRICE)),
    ADMINISTRATOR(Set.of(APARTMENTS_CREATE, APARTMENTS_DELETE, APARTMENTS_ADJUST, APARTMENTS_READ,
            CLIENTS_CREATE, CLIENTS_READ, CLIENTS_DELETE)),
    OWNER(Set.of(Authority.values()));

    private final Set<GrantedAuthority> authorities;

    Role(Set<GrantedAuthority> authorities) {
        this.authorities = authorities;
    }

    public Set<GrantedAuthority> getAuthorities() {
        return authorities;
    }
}
