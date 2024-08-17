package com.hrth.ustock.dto.oauth2;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class CustomOAuth2User implements OAuth2User {

    private final UserOauthDTO userOauthDTO;

    public CustomOAuth2User(UserOauthDTO userOauthDTO) {
        this.userOauthDTO = userOauthDTO;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // role값 return
        Collection<GrantedAuthority> collection = new ArrayList<>();

        collection.add(new SimpleGrantedAuthority(userOauthDTO.getRole()));
        return collection;
    }

    @Override
    public String getName() {
        return userOauthDTO.getName();
    }

    public String getProvider() {
        return userOauthDTO.getProvider();
    }

    public String getProviderId() {
        return userOauthDTO.getProviderId();
    }

    public Long getUserId() {
        return userOauthDTO.getUserId();
    }

}

