package com.andersenlab.securityspringbootstarter;
import org.springframework.security.core.GrantedAuthority;

public enum Authority implements GrantedAuthority {
    APARTMENTS_READ, APARTMENTS_CREATE, APARTMENTS_DELETE, APARTMENTS_ADJUST,
    CLIENTS_READ, CLIENTS_CREATE, CLIENTS_DELETE, CHECK_IN, CHECK_OUT, CALCULATE_PRICE;

    @Override
    public String getAuthority() {
        return this.name();
    }
}
