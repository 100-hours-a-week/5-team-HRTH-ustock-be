package com.hrth.ustock.controller;

import com.hrth.ustock.controller.adapter.UserAdapter;
import com.hrth.ustock.dto.oauth2.UserResponseDto;
import com.hrth.ustock.service.auth.CustomUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/v1/user")
@RequiredArgsConstructor
public class UserController implements UserAdapter {

    private final CustomUserService customUserService;

    @GetMapping
    public ResponseEntity<?> getUserInfo() {

        return ResponseEntity.ok(UserResponseDto.builder()
                .name(customUserService.getCurrentUserDetails().getName())
                .profile(customUserService.getCurrentUserDetails().getProfile())
                .build()
        );
    }
}
