package com.andersenlab.securityspringbootstarter;


import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.Map;


public class HotelJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter original = new org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter();

    public HotelJwtAuthenticationConverter() {
        original.setJwtGrantedAuthoritiesConverter(this::grantedAuthorities);
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt source) {
        return original.convert(source);
    }

    private Collection<GrantedAuthority> grantedAuthorities(Jwt source) {
        Map<String, Collection<String>> realmAccess = source.getClaim("realm_access");
        Collection<String> roles = realmAccess.get("roles");
        return roles.stream()
                .map(role -> Role.valueOf(role.toUpperCase()))
                .flatMap(role -> role.getAuthorities().stream())
                .toList();
    }

}
