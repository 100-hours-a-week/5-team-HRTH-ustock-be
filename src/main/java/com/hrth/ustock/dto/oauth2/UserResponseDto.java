package com.hrth.ustock.dto.oauth2;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponseDto {
    private String name;
    @Schema(
            description = "프로필 이미지 URL"
    )
    private String profile;
}
