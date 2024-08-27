package com.hrth.ustock.controller;

import com.hrth.ustock.dto.oauth2.CustomOAuth2User;
import com.hrth.ustock.dto.oauth2.UserResponseDto;
import io.sentry.Sentry;
import io.sentry.spring.jakarta.EnableSentry;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/v1/user")
public class UserController {

    // 17. 사용자 정보 조회
    @GetMapping
    public ResponseEntity<?> getUserInfo(Authentication authentication) {
        if(authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();

        try{
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
