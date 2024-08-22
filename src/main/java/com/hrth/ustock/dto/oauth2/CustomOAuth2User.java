package com.hrth.ustock.dto.oauth2;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public class CustomOAuth2User implements OAuth2User {

    private final UserOauthDto userOauthDTO;

    @Override
    public Map<String, Object> getAttributes() {
        return null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> collection = new ArrayList<>();
        collection.add(new SimpleGrantedAuthority(userOauthDTO.getRole()));
        return collection;
    }

    @Override
    public String getName() {
        return userOauthDTO.getProviderName();
    }

    public String getProfile() {
        return userOauthDTO.getProfile();
    }

    public Long getUserId() {
        return userOauthDTO.getUserId();
    }

}

