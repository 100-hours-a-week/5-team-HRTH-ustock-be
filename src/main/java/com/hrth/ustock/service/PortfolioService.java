package com.hrth.ustock.service;

import com.hrth.ustock.dto.holding.HoldingEmbedDto;
import com.hrth.ustock.dto.holding.HoldingRequestDto;
import com.hrth.ustock.dto.portfolio.PortfolioListDto;
import com.hrth.ustock.dto.portfolio.PortfolioResponseDto;
import com.hrth.ustock.dto.portfolio.PortfolioUpdateDto;
import com.hrth.ustock.entity.User;
import com.hrth.ustock.entity.portfolio.Holding;
import com.hrth.ustock.entity.portfolio.Portfolio;
import com.hrth.ustock.entity.portfolio.Stock;
import com.hrth.ustock.exception.HoldingNotFoundExeption;
import com.hrth.ustock.exception.PortfolioNotFoundException;
import com.hrth.ustock.exception.StockNotFoundException;
import com.hrth.ustock.exception.UserNotFoundException;
import com.hrth.ustock.repository.HoldingRepository;
import com.hrth.ustock.repository.PortfolioRepository;
import com.hrth.ustock.repository.StockRepository;
import com.hrth.ustock.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PortfolioService {
    private final PortfolioRepository portfolioRepository;
    private final UserRepository userRepository;
    private final HoldingRepository holdingRepository;
    private final StockRepository stockRepository;

    @Transactional(readOnly = true)
    public PortfolioListDto getPortfolioList(Long userId) {
        PortfolioListDto portfolioListDto = new PortfolioListDto();

        List<Portfolio> list = portfolioRepository.findAllByUserUserId(userId);

        if (list == null || list.isEmpty())
            throw new PortfolioNotFoundException();

        list.forEach(p -> {
            // TODO: 현재가 반영
            refreshPortfolio(p, 100000);
            portfolioListDto.setBudget(portfolioListDto.getBudget() + p.getBudget());
            portfolioListDto.setPrincipal(portfolioListDto.getPrincipal() + p.getPrincipal());
            portfolioListDto.setRet(portfolioListDto.getRet() + p.getRet());
            portfolioListDto.getList().add(p.toPortfolioDto());
        });
        Double ror = (double) portfolioListDto.getRet() / portfolioListDto.getPrincipal() * 100;
        ror = (ror.isNaN()) ? 0.0 : ror;
        portfolioListDto.setRor(ror);

        return portfolioListDto;
    }

    @Transactional
    public PortfolioResponseDto getPortfolio(Long pfid) {
        // 수정전까진 현재 정보 기준으로 ror 계산
        // 수정후에는 현재가로 ret 갱신, ror 계산 후 save
        PortfolioResponseDto portfolioResponseDto = new PortfolioResponseDto();
        Portfolio portfolio = portfolioRepository.findById(pfid).orElseThrow(PortfolioNotFoundException::new);
        List<Holding> holdings = portfolio.getHoldings();

        // 포트폴리오 갱신 코드
        // TODO: 현재가 추가 필요
        refreshPortfolio(portfolio, 100000);
        if (holdings == null || holdings.isEmpty())
            return portfolioResponseDto;

        // stocks list
        holdings.forEach(h -> {
            Stock stock = h.getStock();
            int current = 100000;
            HoldingEmbedDto holdingEmbedDto = HoldingEmbedDto.builder()
                    .code(stock.getCode())
                    .name(stock.getName())
                    .quantity(h.getQuantity())
                    .average(h.getAverage())
                    .ror((double) (h.getQuantity() * current) / (h.getQuantity() * h.getAverage()) * 100)
                    .build();
            holdingEmbedDto.setRor(holdingEmbedDto.getRor().isNaN() ? 0.0 : holdingEmbedDto.getRor());
            portfolioResponseDto.getStocks().add(holdingEmbedDto);
        });

        // portfolio data
        portfolioResponseDto.setName(portfolio.getName());
        portfolioResponseDto.setBudget(portfolio.getBudget());
        portfolioResponseDto.setPrincipal(portfolio.getPrincipal());
        portfolioResponseDto.setRet(portfolio.getRet());
        portfolioResponseDto.setRor((double) portfolio.getRet() / portfolio.getPrincipal() * 100);
        portfolioResponseDto.setRor(portfolioResponseDto.getRor().isNaN() ? 0.0 : portfolioResponseDto.getRor());
        return portfolioResponseDto;
    }

    @Transactional
    public ResponseEntity<?> addPortfolio(String name, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        Portfolio portfolio = Portfolio.builder()
                .name(name)
                .user(user)
                .budget(0L)
                .principal(0L)
                .ret(0L)
                .build();
        portfolioRepository.save(portfolio);
        return ResponseEntity.ok().build();
    }

    @Transactional
    public ResponseEntity<?> buyStock(Long pfid, String code, HoldingRequestDto holdingRequestDto) {
        Portfolio portfolio = portfolioRepository.findById(pfid).orElseThrow(PortfolioNotFoundException::new);
        try {
            List<Holding> holdings = portfolio.getHoldings();
            Holding target = null;
            // 보유 종목 중에서 탐색, 없으면 catch 문으로
            for (Holding h : holdings) {
                if (h.getStock().getCode().equals(code)) {
                    target = h;
                }
            }
            if (target == null) {
                throw new HoldingNotFoundExeption();
            } else {
                // 보유중인 종목이면 추가 구매, 반영
                target.additionalBuyHolding(holdingRequestDto.getQuantity(), holdingRequestDto.getPrice());
                holdingRepository.save(target);
            }
        } catch (HoldingNotFoundExeption e) {
            // 보유 종목중에 없으면 새로 등록
            Stock stock = stockRepository.findByCode(code).orElseThrow(StockNotFoundException::new);
            Holding newHolding = Holding.builder()
                    .stock(stock)
                    .portfolio(portfolio)
                    .quantity(holdingRequestDto.getQuantity())
                    .average(holdingRequestDto.getPrice())
                    .build();
            holdingRepository.save(newHolding);

            // 새 종목을 구매하면 포트폴리오 refresh가 제대로 되지 않는 문제 해결
            // TODO: 프론트엔드의 페이지 로드 방식 질문(구매 후 페이지 로드 or not)
            portfolio.getHoldings().add(newHolding);
        }

        // TODO: 현재가 추가
        refreshPortfolio(portfolio, 100000);
        return ResponseEntity.ok().build();
    }

    @Transactional
    protected void refreshPortfolio(Portfolio portfolio, int current) {
        // TODO: 현재가 변경 - holdings의 stock별 현재가
        PortfolioUpdateDto portfolioUpdateDto = new PortfolioUpdateDto();
        List<Holding> list = portfolio.getHoldings();
        // Holdings 현재가 기반 총 자산 및 수익 계산 후 반영
        if (list == null || list.isEmpty()) {
            portfolioUpdateDto.setBudget(portfolio.getBudget());
            portfolio.updatePortfolio(portfolioUpdateDto);
            return;
        }
        // 업데이트
        // budget = 원금+수익, principal = 갯수+평단가, ret = 갯수+현재가
        list.forEach(h -> {
            long before = (long) h.getQuantity() * h.getAverage();
            long after = (long) h.getQuantity() * current;
            portfolioUpdateDto.setPrincipal(portfolioUpdateDto.getPrincipal() + before);
            portfolioUpdateDto.setRet(portfolioUpdateDto.getRet() + after - before);
        });
        portfolioUpdateDto.setBudget(portfolioUpdateDto.getPrincipal() + portfolioUpdateDto.getRet());
        portfolio.updatePortfolio(portfolioUpdateDto);
        portfolioRepository.save(portfolio);
    }
}
