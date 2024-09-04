package com.hrth.ustock.controller.main;

import com.hrth.ustock.controller.adapter.NewsApi;
import com.hrth.ustock.dto.main.news.NewsResponseDto;
import com.hrth.ustock.service.auth.CustomUserService;
import com.hrth.ustock.service.main.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/news")
public class NewsController implements NewsApi {

    private final NewsService newsService;
    private final CustomUserService customUserService;

    @GetMapping("/user")
    public ResponseEntity<List<NewsResponseDto>> myHoldingsNews() {

        List<NewsResponseDto> list = newsService.findHoldingNews(customUserService.getCurrentUserDetails().getUserId());

        return ResponseEntity.ok(list);
    }
}
