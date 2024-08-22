package com.hrth.ustock.controller;

import com.hrth.ustock.dto.news.NewsResponseDto;
import com.hrth.ustock.dto.oauth2.CustomOAuth2User;
import com.hrth.ustock.exception.HoldingNotFoundException;
import com.hrth.ustock.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/v1/news")
public class NewsController {
    private final NewsService newsService;
    private static final long TEMP_USER_ID = 7L;

    // 3. 나만의 뉴스
    @GetMapping("/my")
    public ResponseEntity<?> myHoldingsNews(Authentication authentication) {

        CustomOAuth2User customUserDetails;
        if(authentication != null) {
            customUserDetails = (CustomOAuth2User) authentication.getPrincipal();
        }

        try{
            List<NewsResponseDto> list = newsService.findHoldingNews(TEMP_USER_ID);
            return ResponseEntity.ok(list);
        } catch (HoldingNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
