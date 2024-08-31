package com.hrth.ustock.controller;

import com.hrth.ustock.dto.holding.HoldingRequestDto;
import com.hrth.ustock.dto.oauth2.CustomOAuth2User;
import com.hrth.ustock.dto.portfolio.PortfolioListDto;
import com.hrth.ustock.dto.portfolio.PortfolioRequestDto;
import com.hrth.ustock.dto.portfolio.PortfolioResponseDto;
import com.hrth.ustock.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/portfolio")
public class PortfolioController {
    private final PortfolioService portfolioService;

    @GetMapping
    public ResponseEntity<?> showPortfolioList(@AuthenticationPrincipal CustomOAuth2User customUserDetails) {
        PortfolioListDto list = portfolioService.getPortfolioList(customUserDetails.getUserId());

        return ResponseEntity.ok().body(list);
    }

    @PostMapping
    public ResponseEntity<?> createPortfolio(
            @RequestBody PortfolioRequestDto portfolioRequestDto,
            @AuthenticationPrincipal CustomOAuth2User customUserDetails) {

        portfolioService.addPortfolio(portfolioRequestDto, customUserDetails.getUserId());

        return ResponseEntity.ok().build();
    }

    @GetMapping("/check")
    public ResponseEntity<?> checkUserHasPortfolio(@AuthenticationPrincipal CustomOAuth2User customUserDetails) {
        portfolioService.getPortfolioList(customUserDetails.getUserId());

        return ResponseEntity.ok().build();
    }

    @GetMapping("/{pfId}")
    public ResponseEntity<?> showPortfolioById(@PathVariable("pfId") Long pfId) {
        PortfolioResponseDto portfolio = portfolioService.getPortfolio(pfId);

        return ResponseEntity.ok().body(portfolio);
    }

    @PatchMapping("/{pfId}/holding/{code}")
    public ResponseEntity<?> buyAdditionalPortfolioStock(
            @PathVariable("pfId") Long pfId, @PathVariable("code") String code, @RequestBody HoldingRequestDto holdingRequestDto) {

        portfolioService.additionalBuyStock(pfId, code, holdingRequestDto);

        return ResponseEntity.ok().build();
    }

    @PutMapping("/{pfId}/holding/{code}")
    public ResponseEntity<?> editPortfolioStock(
            @PathVariable("pfId") Long pfId, @PathVariable("code") String code, @RequestBody HoldingRequestDto holdingRequestDto) {

        portfolioService.editHolding(pfId, code, holdingRequestDto);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{pfId}/holding/{code}")
    public ResponseEntity<?> deletePortfolioStock(@PathVariable("pfId") Long pfId, @PathVariable("code") String code) {

        portfolioService.deleteHolding(pfId, code);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{pfId}")
    public ResponseEntity<?> deletePortfolio(@PathVariable("pfId") Long pfId) {

        portfolioService.deletePortfolio(pfId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{pfId}/holding/{code}")
    public ResponseEntity<?> buyPortfolioStock(
            @PathVariable("pfId") Long pfId, @PathVariable("code") String code, @RequestBody HoldingRequestDto holdingRequestDto) {

        portfolioService.buyStock(pfId, code, holdingRequestDto);
        return ResponseEntity.ok().build();
    }
}
