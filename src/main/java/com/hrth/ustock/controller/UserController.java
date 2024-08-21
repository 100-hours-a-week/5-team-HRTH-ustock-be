package com.hrth.ustock.controller;

import com.hrth.ustock.dto.oauth2.CustomOAuth2User;
import com.hrth.ustock.dto.oauth2.UserResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/v1/userInfo")
public class UserController {
    @GetMapping
    public ResponseEntity<?> getUserInfo(Authentication authentication) {
        CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();

        return ResponseEntity.ok(UserResponseDto.builder()
                .name(customUserDetails.getName())
                .picture(customUserDetails.getPicture())
                .build()
        );
    }
}
