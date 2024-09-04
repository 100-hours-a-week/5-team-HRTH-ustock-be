package com.hrth.ustock.controller.main;

import com.hrth.ustock.controller.adapter.PortfolioAdapter;
import com.hrth.ustock.dto.main.holding.HoldingRequestDto;
import com.hrth.ustock.dto.main.portfolio.PortfolioListDto;
import com.hrth.ustock.dto.main.portfolio.PortfolioRequestDto;
import com.hrth.ustock.dto.main.portfolio.PortfolioResponseDto;
import com.hrth.ustock.service.auth.CustomUserService;
import com.hrth.ustock.service.main.PortfolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/portfolio")
public class PortfolioController implements PortfolioAdapter {

    private final PortfolioService portfolioService;
    private final CustomUserService customUserService;

    @GetMapping
    public ResponseEntity<PortfolioListDto> showPortfolioList() {

        PortfolioListDto list = portfolioService.getPortfolioList(customUserService.getCurrentUserDetails().getUserId());

        return ResponseEntity.ok().body(list);
    }

    @PostMapping
    public ResponseEntity<Void> createPortfolio(@RequestBody PortfolioRequestDto portfolioRequestDto) {

        portfolioService.addPortfolio(portfolioRequestDto, customUserService.getCurrentUserDetails().getUserId());

        return ResponseEntity.ok().build();
    }

    @GetMapping("/check")
    public ResponseEntity<Void> checkUserHasPortfolio() {

        portfolioService.getPortfolioList(customUserService.getCurrentUserDetails().getUserId());

        return ResponseEntity.ok().build();
    }

    @GetMapping("/{pfId}")
    public ResponseEntity<PortfolioResponseDto> showPortfolioById(@PathVariable("pfId") Long pfId) {
        PortfolioResponseDto portfolio = portfolioService.getPortfolio(pfId);

        return ResponseEntity.ok().body(portfolio);
    }

    @PatchMapping("/{pfId}/holding/{code}")
    public ResponseEntity<Void> buyAdditionalPortfolioStock(
            @PathVariable("pfId") Long pfId, @PathVariable("code") String code, @RequestBody HoldingRequestDto holdingRequestDto) {

        portfolioService.additionalBuyStock(pfId, code, holdingRequestDto);

        return ResponseEntity.ok().build();
    }

    @PutMapping("/{pfId}/holding/{code}")
    public ResponseEntity<Void> editPortfolioStock(
            @PathVariable("pfId") Long pfId, @PathVariable("code") String code, @RequestBody HoldingRequestDto holdingRequestDto) {

        portfolioService.editHolding(pfId, code, holdingRequestDto);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{pfId}/holding/{code}")
    public ResponseEntity<Void> deletePortfolioStock(@PathVariable("pfId") Long pfId, @PathVariable("code") String code) {

        portfolioService.deleteHolding(pfId, code);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{pfId}")
    public ResponseEntity<Void> deletePortfolio(@PathVariable("pfId") Long pfId) {

        portfolioService.deletePortfolio(pfId);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{pfId}/holding/{code}")
    public ResponseEntity<Void> buyPortfolioStock(
            @PathVariable("pfId") Long pfId, @PathVariable("code") String code, @RequestBody HoldingRequestDto holdingRequestDto) {

        portfolioService.buyStock(pfId, code, holdingRequestDto);

        return ResponseEntity.ok().build();
    }
}
