package com.hrth.ustock.controller;

import com.hrth.ustock.dto.holding.HoldingRequestDto;
import com.hrth.ustock.dto.portfolio.PortfolioListDto;
import com.hrth.ustock.dto.portfolio.PortfolioResponseDto;
import com.hrth.ustock.exception.HoldingNotFoundExeption;
import com.hrth.ustock.exception.PortfolioNotFoundException;
import com.hrth.ustock.exception.StockNotFoundException;
import com.hrth.ustock.exception.UserNotFoundException;
import com.hrth.ustock.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/portfolio")
public class PortfolioController {
    private final PortfolioService portfolioService;

    // 7. 포트폴리오 생성
    @PostMapping
    public ResponseEntity<?> createPortfolio(String name) {

        // TODO: 스프링 시큐리티로 유저 아이디 받아서 넘기기 - 현재 임시로
        try {
            return portfolioService.addPortfolio(name, 1L);
        } catch (UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 8. 포트폴리오 리스트 조회
    @GetMapping
    public ResponseEntity<?> showPortfolioList() {

        // TODO: 스프링 시큐리티로 유저 아이디 받아서 넘기기 userId
        try {
            PortfolioListDto list = portfolioService.getPortfolioList(1L);
            return ResponseEntity.ok().body(list);
        } catch (PortfolioNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 9. 개별 포트폴리오 조회
    @GetMapping("/{pfid}")
    public ResponseEntity<?> showPortfolioById(@PathVariable("pfid") Long pfid) {
        try {
            PortfolioResponseDto portfolio = portfolioService.getPortfolio(pfid);
            return ResponseEntity.ok().body(portfolio);
        } catch (PortfolioNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 10. 개별 종목 추가 매수
    @PatchMapping("/{pfid}/{code}")
    public ResponseEntity<?> buyPortfolioStock(@PathVariable("pfid") Long pfid, @PathVariable("code") String code,
                                               @RequestBody HoldingRequestDto holdingRequestDto) {
        // 갯수, 현재가 예외처리
        if (holdingRequestDto.getQuantity() <= 0 || holdingRequestDto.getPrice() <= 0) {
            return ResponseEntity.badRequest().build();
        }
        try {
            return portfolioService.buyStock(pfid, code, holdingRequestDto);
        } catch (PortfolioNotFoundException | StockNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            // argument not match
            return ResponseEntity.badRequest().build();
        }
    }

    // 11. 개별 종목 수정
    @PutMapping("/{pfid}/{code}")
    public ResponseEntity<?> editPortfolioStock(@PathVariable("pfid") Long pfid, @PathVariable("code") String code,
                                                @RequestBody HoldingRequestDto holdingRequestDto) {
        // 갯수, 현재가 예외처리
        if (holdingRequestDto.getQuantity() < 0 || holdingRequestDto.getPrice() < 0) {
            return ResponseEntity.badRequest().build();
        }
        try{
            return portfolioService.editStock(pfid, code, holdingRequestDto);
        } catch (HoldingNotFoundExeption | StockNotFoundException | PortfolioNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

}
