package com.hrth.ustock.dto.oauth2;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserOauthDTO {
    private long userId;
    private String role;
    private String provider;
    private String providerId;
    private String providerName;
    private String picture;
}
