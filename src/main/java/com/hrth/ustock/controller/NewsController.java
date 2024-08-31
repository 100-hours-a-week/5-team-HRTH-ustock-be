package com.hrth.ustock.controller;

import com.hrth.ustock.dto.news.NewsResponseDto;
import com.hrth.ustock.dto.oauth2.CustomOAuth2User;
import com.hrth.ustock.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/v1/news")
public class NewsController {
    private final NewsService newsService;

    @GetMapping("/user")
    public ResponseEntity<?> myHoldingsNews(@AuthenticationPrincipal CustomOAuth2User customUserDetails) {
        List<NewsResponseDto> list = newsService.findHoldingNews(customUserDetails.getUserId());

        return ResponseEntity.ok(list);
    }
}
