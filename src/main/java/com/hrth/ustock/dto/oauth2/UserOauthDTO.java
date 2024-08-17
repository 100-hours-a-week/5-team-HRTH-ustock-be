package com.hrth.ustock.dto.oauth2;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserOauthDTO {
    private Long userId;
    private String role;
    private String provider;
    private String providerId;
    private String name;
    private String email;
    private String picture;
}
