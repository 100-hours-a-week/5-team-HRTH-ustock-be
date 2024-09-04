package com.hrth.ustock.controller.adapter;

import com.hrth.ustock.dto.main.holding.HoldingRequestDto;
import com.hrth.ustock.dto.main.portfolio.PortfolioListDto;
import com.hrth.ustock.dto.main.portfolio.PortfolioRequestDto;
import com.hrth.ustock.dto.main.portfolio.PortfolioResponseDto;
import com.hrth.ustock.exception.common.ExceptionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "Portfolio", description = "포트폴리오 관련 API")
public interface PortfolioAdapter {

    @Operation(
            summary = "보유 포트폴리오 리스트 조회",
            description = "사용자가 보유중인 포트폴리오 리스트 반환"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    content = @Content(schema = @Schema(implementation = PortfolioListDto.class))),
            @ApiResponse(
                    responseCode = "401",
                    description = "해당 사용자를 찾을 수 없습니다.",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "보유중인 포트폴리오가 없습니다.",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버에 오류가 발생하였습니다.",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class)))
    })
    ResponseEntity<PortfolioListDto> showPortfolioList();

    @Operation(
            summary = "포트폴리오 생성",
            description = "사용자의 새 포트폴리오 생성"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "포트폴리오 생성 성공"),
            @ApiResponse(
                    responseCode = "401",
                    description = "해당 사용자를 찾을 수 없습니다.",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(
                    responseCode = "409",
                    description = "이미 존재하는 포트폴리오 이름입니다.",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버에 오류가 발생하였습니다.",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class)))
    })
    ResponseEntity<Void> createPortfolio(
            @RequestBody(
                    description = "생성할 포트폴리오 정보",
                    required = true,
                    content = @Content(schema = @Schema(implementation = PortfolioRequestDto.class))
            ) PortfolioRequestDto portfolioRequestDto);

    @Operation(
            summary = "포트폴리오 보유 여부 확인",
            description = "사용자의 포트폴리오 보유 여부 반환"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "보유중인 포트폴리오가 있습니다"),
            @ApiResponse(
                    responseCode = "204",
                    description = "보유중인 포트폴리오가 없습니다.",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(
                    responseCode = "401",
                    description = "해당 사용자를 찾을 수 없습니다.",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버에 오류가 발생하였습니다.",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class)))
    })
    ResponseEntity<Void> checkUserHasPortfolio();

    @Operation(
            summary = "포트폴리오 조회",
            description = "사용자의 특정 포트폴리오 조회"
    )
    @Parameter(
            name = "pfId",
            description = "포트폴리오 아이디",
            required = true
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "보유중인 포트폴리오가 있습니다"),
            @ApiResponse(
                    responseCode = "401",
                    description = "로그인 후 이용 가능.",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "보유중인 포트폴리오가 없습니다.",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버에 오류가 발생하였습니다.",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class)))
    })
    ResponseEntity<PortfolioResponseDto> showPortfolioById(
            @PathVariable("pfId") Long pfId
    );

    @Operation(
            summary = "종목 매수",
            description = "특정 포트폴리오 새 종목 매수"
    )
    @Parameters({
            @Parameter(
                    name = "pfId",
                    description = "포트폴리오 아이디",
                    required = true),
            @Parameter(
                    name = "code",
                    description = "추가 매수할 보유 종목 코드",
                    required = true)
    })
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "종목 추가매수 성공"),
            @ApiResponse(
                    responseCode = "400",
                    description = "입력값이 범위를 초과하였습니다.",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(
                    responseCode = "401",
                    description = "로그인 후 이용 가능.",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "해당 포트폴리오를 찾을 수 없습니다.",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "해당 종목을 찾을 수 없습니다.",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버에 오류가 발생하였습니다.",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class)))
    })
    public ResponseEntity<Void> buyPortfolioStock(
            @PathVariable("pfId") Long pfId,
            @PathVariable("code") String code,
            @RequestBody HoldingRequestDto holdingRequestDto
    );

    @Operation(
            summary = "종목 추가 매수",
            description = "특정 포트폴리오 보유종목 추가매수"
    )
    @Parameters({
            @Parameter(
                    name = "pfId",
                    description = "포트폴리오 아이디",
                    required = true),
            @Parameter(
                    name = "code",
                    description = "추가 매수할 보유 종목 코드",
                    required = true)
    })
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "종목 추가매수 성공"),
            @ApiResponse(
                    responseCode = "400",
                    description = "입력값이 범위를 초과하였습니다.",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(
                    responseCode = "401",
                    description = "로그인 후 이용 가능.",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "보유 종목 정보를 찾을 수 없습니다.",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버에 오류가 발생하였습니다.",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class)))
    })
    ResponseEntity<Void> buyAdditionalPortfolioStock(
            @PathVariable("pfId") Long pfId,
            @PathVariable("code") String code,
            @RequestBody HoldingRequestDto holdingRequestDto
    );

    @Operation(
            summary = "종목 수정",
            description = "특정 포트폴리오 보유종목 수정"
    )
    @Parameters({
            @Parameter(
                    name = "pfId",
                    description = "포트폴리오 아이디",
                    required = true),
            @Parameter(
                    name = "code",
                    description = "수정할 보유 종목 코드",
                    required = true)
    })
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "종목 수정 성공"),
            @ApiResponse(
                    responseCode = "400",
                    description = "입력값이 범위를 초과하였습니다.",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(
                    responseCode = "401",
                    description = "로그인 후 이용 가능.",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "보유 종목 정보를 찾을 수 없습니다.",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버에 오류가 발생하였습니다.",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class)))
    })
    ResponseEntity<Void> editPortfolioStock(
            @PathVariable("pfId") Long pfId,
            @PathVariable("code") String code,
            @RequestBody HoldingRequestDto holdingRequestDto
    );

    @Operation(
            summary = "보유 종목 삭제",
            description = "특정 포트폴리오 보유종목 삭제"
    )
    @Parameters({
            @Parameter(
                    name = "pfId",
                    description = "포트폴리오 아이디",
                    required = true),
            @Parameter(
                    name = "code",
                    description = "삭제할 보유 종목 코드",
                    required = true)
    })
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "보유종목 삭제 성공"),
            @ApiResponse(
                    responseCode = "401",
                    description = "로그인 후 이용 가능.",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "해당 포트폴리오를 찾을 수 없습니다.",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "보유 종목 정보를 찾을 수 없습니다.",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버에 오류가 발생하였습니다.",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class)))
    })
    ResponseEntity<Void> deletePortfolioStock(
            @PathVariable("pfId") Long pfId,
            @PathVariable("code") String code
    );

    @Operation(
            summary = "포트폴리오 삭제",
            description = "특정 포트폴리오 삭제"
    )
    @Parameter(
            name = "pfId",
            description = "포트폴리오 아이디",
            required = true
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "포트폴리오 삭제 성공"),
            @ApiResponse(
                    responseCode = "401",
                    description = "로그인 후 이용 가능.",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "해당 포트폴리오를 찾을 수 없습니다.",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버에 오류가 발생하였습니다.",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class)))
    })
    ResponseEntity<Void> deletePortfolio(
            @PathVariable("pfId") Long pfId
    );
}
