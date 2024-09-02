package com.hrth.ustock.controller.main;

import com.hrth.ustock.dto.main.news.NewsResponseDto;
import com.hrth.ustock.dto.oauth2.CustomOAuth2User;
import com.hrth.ustock.service.main.NewsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "News", description = "뉴스 관련 API")
public class NewsController {
    private final NewsService newsService;

    // 3. 나만의 뉴스
    @GetMapping("/user")
    @Operation(summary = "나만의 뉴스 조회", description = "사용자가 보유 종목을 가지고 있다면 해당 종목에 대한 뉴스를 반환")
    @ApiResponse(responseCode = "200",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = NewsResponseDto.class))
            )
    )
    public ResponseEntity<?> myHoldingsNews(@AuthenticationPrincipal CustomOAuth2User customUserDetails) {

        List<NewsResponseDto> list = newsService.findHoldingNews(customUserDetails.getUserId());

        return ResponseEntity.ok(list);
    }
}
