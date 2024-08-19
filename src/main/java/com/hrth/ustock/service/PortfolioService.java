package com.hrth.ustock.service;

import com.hrth.ustock.dto.holding.HoldingEmbedDto;
import com.hrth.ustock.dto.holding.HoldingRequestDto;
import com.hrth.ustock.dto.portfolio.PortfolioListDto;
import com.hrth.ustock.dto.portfolio.PortfolioRequestDTO;
import com.hrth.ustock.dto.portfolio.PortfolioResponseDto;
import com.hrth.ustock.dto.portfolio.PortfolioUpdateDto;
import com.hrth.ustock.entity.User;
import com.hrth.ustock.entity.portfolio.Holding;
import com.hrth.ustock.entity.portfolio.Portfolio;
import com.hrth.ustock.entity.portfolio.Stock;
import com.hrth.ustock.exception.HoldingNotFoundException;
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
    private static final int tempCurrent = 100000;

    @Transactional(readOnly = true)
    public PortfolioListDto getPortfolioList(Long userId) {

        PortfolioListDto portfolioListDto = new PortfolioListDto();
        List<Portfolio> list = portfolioRepository.findAllByUserUserId(userId);

        if (list == null || list.isEmpty())
            throw new PortfolioNotFoundException();

        list.forEach(p -> {
            // TODO: 현재가 반영
            refreshPortfolio(p, tempCurrent);
            portfolioListDto.setBudget(portfolioListDto.getBudget() + p.getBudget());
            portfolioListDto.setPrincipal(portfolioListDto.getPrincipal() + p.getPrincipal());
            portfolioListDto.setRet(portfolioListDto.getRet() + p.getRet());
            portfolioListDto.getList().add(p.toPortfolioDto());
        });
        double ror = (portfolioListDto.getPrincipal()==0L) ? 0.0 : (double) portfolioListDto.getRet() / portfolioListDto.getPrincipal() * 100;
        portfolioListDto.setRor(ror);

        return portfolioListDto;
    }

    @Transactional
    public PortfolioResponseDto getPortfolio(Long pfId) {

        PortfolioResponseDto portfolioResponseDto = new PortfolioResponseDto();
        Portfolio portfolio = portfolioRepository.findById(pfId).orElseThrow(PortfolioNotFoundException::new);
        List<Holding> holdings = portfolio.getHoldings();

        // TODO: 현재가 추가 필요
        refreshPortfolio(portfolio, tempCurrent);
        if (holdings == null || holdings.isEmpty()) {
            portfolioResponseDto.setName(portfolio.getName());
            return portfolioResponseDto;
        }

        // 현재가 반영전까진 현재 정보 기준으로 ror 계산
        // 현재가 반영후에는 현재가로 ret 갱신, ror 계산 후 save
        holdings.forEach(h -> {
            Stock stock = h.getStock();
            int ret = h.getQuantity() * tempCurrent - h.getQuantity() * h.getAverage();
            HoldingEmbedDto holdingEmbedDto = HoldingEmbedDto.builder()
                    .code(stock.getCode())
                    .name(stock.getName())
                    .quantity(h.getQuantity())
                    .average(h.getAverage())
                    .ror((h.getQuantity()*h.getAverage()==0) ? 0.0 : (double)(ret / (h.getQuantity() * h.getAverage())) * 100)
                    .build();
            portfolioResponseDto.getStocks().add(holdingEmbedDto);
        });

        portfolioResponseDto.setName(portfolio.getName());
        portfolioResponseDto.setBudget(portfolio.getBudget());
        portfolioResponseDto.setPrincipal(portfolio.getPrincipal());
        portfolioResponseDto.setRet(portfolio.getRet());
        portfolioResponseDto.setRor((portfolio.getPrincipal()==0L) ? 0.0 : (double) portfolio.getRet() / portfolio.getPrincipal() * 100);
        return portfolioResponseDto;
    }

    @Transactional
    public void addPortfolio(PortfolioRequestDTO portfolioRequestDTO, Long userId) {

        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        Portfolio portfolio = Portfolio.builder()
                .name(portfolioRequestDTO.getName())
                .user(user)
                .budget(0L)
                .principal(0L)
                .ret(0L)
                .build();
        portfolioRepository.save(portfolio);
    }

    // 10. 개별 종목 추가 매수
    @Transactional
    public void buyStock(Long pfId, String code, HoldingRequestDto holdingRequestDto) {

        Portfolio portfolio = portfolioRepository.findById(pfId).orElseThrow(PortfolioNotFoundException::new);
        List<Holding> holdings = portfolio.getHoldings();

        Holding target = null;
        // 보유 종목 중에서 탐색 후 매수, 없으면 새로운 보유 종목 추가
        for (Holding h : holdings) {
            if (h.getStock().getCode().equals(code)) {
                target = h;
            }
        }
        if (target != null) {
            target.additionalBuyHolding(holdingRequestDto.getQuantity(), holdingRequestDto.getPrice());
            holdingRepository.save(target);
        } else {
            Stock stock = stockRepository.findByCode(code).orElseThrow(StockNotFoundException::new);
            Holding newHolding = Holding.builder()
                    .stock(stock)
                    .portfolio(portfolio)
                    .quantity(holdingRequestDto.getQuantity())
                    .average(holdingRequestDto.getPrice())
                    .build();
            holdingRepository.save(newHolding);
        }
    }

    // 11. 개별 종목 수정
    @Transactional
    public void editHolding(Long pfId, String code, HoldingRequestDto holdingRequestDto) {

        Portfolio portfolio = portfolioRepository.findById(pfId).orElseThrow(PortfolioNotFoundException::new);
        List<Holding> holdings = portfolio.getHoldings();

        Holding target = null;
        // 보유 종목 중에서 탐색 후 있으면 삭제, 없으면 throw exception
        for (Holding h : holdings) {
            if (h.getStock().getCode().equals(code)) {
                target = h;
            }
        }
        if (target != null) {
            // input: quantity, price = 0일시 해당 보유종목 삭제
            if (holdingRequestDto.getQuantity() == 0 || holdingRequestDto.getPrice() == 0) {
                holdingRepository.delete(target);
                portfolio.getHoldings().remove(target);
            } else {
                target.updateHolding(holdingRequestDto.getQuantity(), holdingRequestDto.getPrice());
                holdingRepository.save(target);
            }
        } else {
            throw new HoldingNotFoundException();
        }

        // TODO: 현재가 추가
        refreshPortfolio(portfolio, tempCurrent);
    }

    // 12. 개별 종목 삭제
    @Transactional
    public void deleteHolding(Long pfId, String code) {

        Portfolio portfolio = portfolioRepository.findById(pfId).orElseThrow(PortfolioNotFoundException::new);
        List<Holding> holdings = portfolio.getHoldings();

        Holding target = null;
        // 보유 종목 중에서 탐색 후 있으면 삭제, 없으면 throw exception
        for (Holding h : holdings) {
            if (h.getStock().getCode().equals(code)) {
                target = h;
            }
        }
        if (target != null) {
            holdingRepository.delete(target);
            portfolio.getHoldings().remove(target);
        } else {
            throw new HoldingNotFoundException();
        }
    }

    // 13. 포트폴리오 삭제
    @Transactional
    public void deletePortfolio(Long pfId) {

        Portfolio portfolio = portfolioRepository.findById(pfId).orElseThrow(PortfolioNotFoundException::new);
        List<Holding> list = portfolio.getHoldings();

        // 보유 종목 없을시 바로 삭제, 아니면 보유종목 전부 삭제
        if (list == null || list.isEmpty()) {
            portfolioRepository.delete(portfolio);
        } else {
            holdingRepository.deleteAll(list);
            portfolioRepository.delete(portfolio);
        }
    }

    @Transactional
    protected void refreshPortfolio(Portfolio portfolio, int current) {

        // TODO: 현재가 변경 - holdings의 stock별 현재가
        PortfolioUpdateDto portfolioUpdateDto = new PortfolioUpdateDto();
        List<Holding> list = portfolio.getHoldings();

        if (list == null || list.isEmpty()) {
            portfolioUpdateDto.setBudget(portfolio.getBudget());
            portfolio.updatePortfolio(portfolioUpdateDto);
            return;
        }

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
