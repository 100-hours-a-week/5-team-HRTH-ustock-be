package com.hrth.ustock.controller.main;

import com.hrth.ustock.dto.main.news.NewsResponseDto;
import com.hrth.ustock.service.auth.CustomUserService;
import com.hrth.ustock.service.main.NewsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/news")
@Tag(name = "News", description = "뉴스 관련 API")
public class NewsController {

    private final NewsService newsService;
    private final CustomUserService customUserService;

    // 3. 나만의 뉴스
    @GetMapping("/user")
    @Operation(summary = "나만의 뉴스 조회", description = "사용자가 보유 종목을 가지고 있다면 해당 종목에 대한 뉴스를 반환")
    public ResponseEntity<List<NewsResponseDto>> myHoldingsNews() {

        List<NewsResponseDto> list = newsService.findHoldingNews(customUserService.getCurrentUserDetails().getUserId());

        return ResponseEntity.ok(list);
    }

    @GetMapping("/{code}")
    @Operation(summary = "종목별 뉴스 조회", description = "종목 코드를 바탕으로 해당하는 종목의 뉴스 리스트를 반환")
    public ResponseEntity<List<NewsResponseDto>> showHoldingNews(@PathVariable String code) {

        List<NewsResponseDto> newsList = new ArrayList<>();

        return ResponseEntity.ok(newsList);
    }
}
