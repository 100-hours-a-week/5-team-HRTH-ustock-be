package com.hrth.ustock.controller.api;

import com.hrth.ustock.dto.oauth2.UserResponseDto;
import com.hrth.ustock.exception.common.ExceptionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "User", description = "유저 관련 API")
public interface UserApi {
    @Operation(
            summary = "사용자 정보 조회",
            description = "구글 계정 사용자 이름, 프로필 사진 반환"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(
                    responseCode = "401",
                    description = "로그인 되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "해당 사용자를 찾을 수 없습니다.",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버에 오류가 발생하였습니다.",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class)))
    })
    ResponseEntity<?> getUserInfo();
}
