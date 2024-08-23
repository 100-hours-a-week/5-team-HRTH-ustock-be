package com.hrth.ustock.dto.oauth2;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponseDto {
    private String name;
    private String profile;
}
