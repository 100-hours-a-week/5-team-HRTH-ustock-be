package com.hrth.ustock.controller;

import com.hrth.ustock.dto.oauth2.CustomOAuth2User;
import com.hrth.ustock.dto.oauth2.UserResponseDto;
import io.sentry.Sentry;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/v1/user")
public class UserController {

    @GetMapping
    public ResponseEntity<?> getUserInfo(@AuthenticationPrincipal CustomOAuth2User customUserDetails) {
        try {
            return ResponseEntity.ok(UserResponseDto.builder()
                    .name(customUserDetails.getName())
                    .profile(customUserDetails.getProfile())
                    .build()
            );
        } catch (Exception e) {
            Sentry.captureException(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
