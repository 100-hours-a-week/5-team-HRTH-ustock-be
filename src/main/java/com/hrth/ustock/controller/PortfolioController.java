package com.hrth.ustock.controller;

import com.hrth.ustock.dto.holding.HoldingRequestDto;
import com.hrth.ustock.dto.oauth2.CustomOAuth2User;
import com.hrth.ustock.dto.portfolio.PortfolioListDto;
import com.hrth.ustock.dto.portfolio.PortfolioRequestDto;
import com.hrth.ustock.dto.portfolio.PortfolioResponseDto;
import com.hrth.ustock.exception.*;
import com.hrth.ustock.service.PortfolioService;
import io.sentry.Sentry;
import io.sentry.spring.jakarta.EnableSentry;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/portfolio")
public class PortfolioController {
    private final PortfolioService portfolioService;

    @GetMapping
    public ResponseEntity<?> showPortfolioList(Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();

        try {
            PortfolioListDto list = portfolioService.getPortfolioList(customUserDetails.getUserId());
            return ResponseEntity.ok().body(list);
        } catch (PortfolioNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            Sentry.captureException(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<?> createPortfolio(@RequestBody PortfolioRequestDto portfolioRequestDto, Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();

        try {
            portfolioService.addPortfolio(portfolioRequestDto, customUserDetails.getUserId());
            return ResponseEntity.ok().build();
        } catch (UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (ExistPortfolioNameException e) {
            return ResponseEntity.badRequest().body("이미 존재하는 포트폴리오 이름입니다.");
        } catch (Exception e) {
            Sentry.captureException(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/check")
    public ResponseEntity<?> checkUserHasPortfolio(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();

        try {
            portfolioService.getPortfolioList(customUserDetails.getUserId());
        } catch (PortfolioNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
        }
        return ResponseEntity.ok().body(true);
    }

    @GetMapping("/{pfId}")
    public ResponseEntity<?> showPortfolioById(@PathVariable("pfId") Long pfId) {

        try {
            PortfolioResponseDto portfolio = portfolioService.getPortfolio(pfId);
            return ResponseEntity.ok().body(portfolio);
        } catch (PortfolioNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            Sentry.captureException(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PatchMapping("/{pfId}/holding/{code}")
    public ResponseEntity<?> buyAdditionalPortfolioStock(
            @PathVariable("pfId") Long pfId, @PathVariable("code") String code, @RequestBody HoldingRequestDto holdingRequestDto) {

        if (holdingRequestDto.getQuantity() <= 0 || holdingRequestDto.getPrice() <= 0) {
            return ResponseEntity.badRequest().build();
        }

        try {
            portfolioService.additionalBuyStock(pfId, code, holdingRequestDto);
            return ResponseEntity.ok().build();
        } catch (PortfolioNotFoundException | StockNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (InputNotValidException e) {
            return ResponseEntity.badRequest().body("입력값이 범위를 초과하였습니다.");
        } catch (Exception e) {
            Sentry.captureException(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{pfId}/holding/{code}")
    public ResponseEntity<?> editPortfolioStock(
            @PathVariable("pfId") Long pfId, @PathVariable("code") String code, @RequestBody HoldingRequestDto holdingRequestDto) {

        if (holdingRequestDto.getQuantity() < 0 || holdingRequestDto.getPrice() < 0) {
            return ResponseEntity.badRequest().build();
        }

        try {
            portfolioService.editHolding(pfId, code, holdingRequestDto);
        } catch (HoldingNotFoundException | PortfolioNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (InputNotValidException e) {
            return ResponseEntity.badRequest().body("입력값이 범위를 초과하였습니다.");
        } catch (Exception e) {
            Sentry.captureException(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{pfId}/holding/{code}")
    public ResponseEntity<?> deletePortfolioStock(@PathVariable("pfId") Long pfId, @PathVariable("code") String code) {

        try {
            portfolioService.deleteHolding(pfId, code);
        } catch (HoldingNotFoundException | PortfolioNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            Sentry.captureException(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{pfId}")
    public ResponseEntity<?> deletePortfolio(@PathVariable("pfId") Long pfId) {

        try {
            portfolioService.deletePortfolio(pfId);
        } catch (PortfolioNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            Sentry.captureException(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{pfId}/holding/{code}")
    public ResponseEntity<?> buyPortfolioStock(
            @PathVariable("pfId") Long pfId, @PathVariable("code") String code, @RequestBody HoldingRequestDto holdingRequestDto) {

        try {
            portfolioService.buyStock(pfId, code, holdingRequestDto);
        } catch (PortfolioNotFoundException | StockNotFoundException e) {
            String message = e instanceof PortfolioNotFoundException ?
                    "포트폴리오 정보를 찾을 수 없습니다." :
                    "주식 정보를 찾을 수 없습니다.";

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
        } catch (InputNotValidException e) {
            return ResponseEntity.badRequest().body("입력값이 범위를 초과하였습니다.");
        } catch (Exception e) {
            Sentry.captureException(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        return ResponseEntity.ok().build();
    }
}
