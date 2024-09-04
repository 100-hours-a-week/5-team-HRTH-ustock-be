package com.hrth.ustock.controller.adapter;

import com.hrth.ustock.dto.main.news.NewsResponseDto;
import com.hrth.ustock.exception.common.ExceptionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "News", description = "뉴스 관련 API")
public interface NewsAdapter {

    @Operation(
            summary = "나만의 뉴스 조회",
            description = "사용자가 보유 종목을 가지고 있다면 해당 종목에 대한 뉴스를 반환"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    content = @Content(schema = @Schema(implementation = NewsResponseDto.class))),
            @ApiResponse(
                    responseCode = "401",
                    description = "해당 사용자를 찾을 수 없습니다.",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "보유중인 종목이 없습니다.",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버에 오류가 발생하였습니다.",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class)))
    })
    ResponseEntity<?> myHoldingsNews();
}
