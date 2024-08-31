package com.hrth.ustock.controller;

import com.hrth.ustock.dto.oauth2.CustomOAuth2User;
import com.hrth.ustock.dto.oauth2.UserResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/v1/user")
@Tag(name = "User", description = "유저 관련 API")
public class UserController {

    @GetMapping
    @Operation(summary = "사용자 정보 조회", description = "구글 계정 사용자 이름, 프로필 사진 반환")
    @ApiResponse(responseCode = "200",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserResponseDto.class)
            )
    )
    public ResponseEntity<?> getUserInfo(@AuthenticationPrincipal CustomOAuth2User customUserDetails) {
        return ResponseEntity.ok(UserResponseDto.builder()
                .name(customUserDetails.getName())
                .profile(customUserDetails.getProfile())
                .build()
        );
    }
}
