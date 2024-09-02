package com.hrth.ustock.controller.main;

import com.hrth.ustock.dto.main.holding.HoldingRequestDto;
import com.hrth.ustock.dto.oauth2.CustomOAuth2User;
import com.hrth.ustock.dto.main.portfolio.PortfolioListDto;
import com.hrth.ustock.dto.main.portfolio.PortfolioRequestDto;
import com.hrth.ustock.dto.main.portfolio.PortfolioResponseDto;
import com.hrth.ustock.service.main.PortfolioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/portfolio")
@Tag(name = "Portfolio", description = "포트폴리오 관련 API")
public class PortfolioController {
    private final PortfolioService portfolioService;

    @GetMapping
    @Operation(summary = "보유 포트폴리오 리스트 조회", description = "사용자가 보유중인 포트폴리오 리스트 반환")
    @ApiResponse(responseCode = "200",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PortfolioListDto.class)
            )
    )
    public ResponseEntity<?> showPortfolioList(@AuthenticationPrincipal CustomOAuth2User customUserDetails) {
        PortfolioListDto list = portfolioService.getPortfolioList(customUserDetails.getUserId());

        return ResponseEntity.ok().body(list);
    }

    @PostMapping
    @Operation(summary = "포트폴리오 생성", description = "사용자의 새 포트폴리오 생성")
    public ResponseEntity<?> createPortfolio(
            @RequestBody PortfolioRequestDto portfolioRequestDto,
            @AuthenticationPrincipal CustomOAuth2User customUserDetails) {

        portfolioService.addPortfolio(portfolioRequestDto, customUserDetails.getUserId());

        return ResponseEntity.ok().build();
    }

    @GetMapping("/check")
    @Operation(summary = "포트폴리오 보유 여부 확인", description = "사용자의 포트폴리오 보유 여부 반환")
    public ResponseEntity<?> checkUserHasPortfolio(@AuthenticationPrincipal CustomOAuth2User customUserDetails) {
        portfolioService.getPortfolioList(customUserDetails.getUserId());

        return ResponseEntity.ok().build();
    }

    @GetMapping("/{pfId}")
    @Operation(summary = "포트폴리오 조회", description = "사용자의 특정 포트폴리오 조회")
    @ApiResponse(responseCode = "200",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PortfolioResponseDto.class)
            )
    )
    public ResponseEntity<?> showPortfolioById(@PathVariable("pfId") Long pfId) {
        PortfolioResponseDto portfolio = portfolioService.getPortfolio(pfId);

        return ResponseEntity.ok().body(portfolio);
    }

    @PatchMapping("/{pfId}/holding/{code}")
    @Operation(summary = "종목 추가 매수", description = "특정 포트폴리오 보유종목 추가매수")
    public ResponseEntity<?> buyAdditionalPortfolioStock(
            @PathVariable("pfId") Long pfId, @PathVariable("code") String code, @RequestBody HoldingRequestDto holdingRequestDto) {

        portfolioService.additionalBuyStock(pfId, code, holdingRequestDto);

        return ResponseEntity.ok().build();
    }

    @PutMapping("/{pfId}/holding/{code}")
    @Operation(summary = "종목 수정", description = "특정 포트폴리오 보유종목 수정")
    public ResponseEntity<?> editPortfolioStock(
            @PathVariable("pfId") Long pfId, @PathVariable("code") String code, @RequestBody HoldingRequestDto holdingRequestDto) {

        portfolioService.editHolding(pfId, code, holdingRequestDto);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{pfId}/holding/{code}")
    @Operation(summary = "보유 종목 삭제", description = "특정 포트폴리오 보유종목 삭제")
    public ResponseEntity<?> deletePortfolioStock(@PathVariable("pfId") Long pfId, @PathVariable("code") String code) {

        portfolioService.deleteHolding(pfId, code);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{pfId}")
    @Operation(summary = "포트폴리오 삭제", description = "특정 포트폴리오 삭제")
    public ResponseEntity<?> deletePortfolio(@PathVariable("pfId") Long pfId) {

        portfolioService.deletePortfolio(pfId);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{pfId}/holding/{code}")
    @Operation(summary = "종목 매수", description = "특정 포트폴리오 새 종목 매수")
    public ResponseEntity<?> buyPortfolioStock(
            @PathVariable("pfId") Long pfId, @PathVariable("code") String code, @RequestBody HoldingRequestDto holdingRequestDto) {

        portfolioService.buyStock(pfId, code, holdingRequestDto);

        return ResponseEntity.ok().build();
    }
}
