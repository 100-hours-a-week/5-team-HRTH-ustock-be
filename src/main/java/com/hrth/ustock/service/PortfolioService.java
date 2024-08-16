package com.hrth.ustock.service;

import com.hrth.ustock.dto.holding.HoldingEmbedDto;
import com.hrth.ustock.dto.portfolio.PortfolioListDto;
import com.hrth.ustock.dto.portfolio.PortfolioResponseDto;
import com.hrth.ustock.dto.portfolio.PortfolioUpdateDto;
import com.hrth.ustock.entity.User;
import com.hrth.ustock.entity.portfolio.Holding;
import com.hrth.ustock.entity.portfolio.Portfolio;
import com.hrth.ustock.entity.portfolio.Stock;
import com.hrth.ustock.exception.HoldingNotFoundExeption;
import com.hrth.ustock.exception.PortfolioNotFoundException;
import com.hrth.ustock.exception.UserNotFoundExeption;
import com.hrth.ustock.repository.HoldingRepository;
import com.hrth.ustock.repository.PortfolioRepository;
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

    @Transactional
    protected void refreshPortfolio(Portfolio portfolio, int current) {
        PortfolioUpdateDto portfolioUpdateDto = new PortfolioUpdateDto();
        List<Holding> list = portfolio.getHoldings();
        if (list == null || list.isEmpty()) {
            portfolioUpdateDto.setBudget(portfolio.getBudget());
            portfolio.updatePortfolio(portfolioUpdateDto);
            return;
        }

        list.forEach(h -> {
            long before = (long)h.getQuantity()*h.getAverage();
            long after = (long)h.getQuantity()*current;
            portfolioUpdateDto.setPrincipal(portfolio.getPrincipal()+before);
            portfolioUpdateDto.setRet(portfolio.getRet()+after-before);
        });
        portfolioUpdateDto.setBudget(portfolioUpdateDto.getPrincipal()+ portfolioUpdateDto.getRet());
        portfolio.updatePortfolio(portfolioUpdateDto);
        portfolioRepository.save(portfolio);
    }


    @Transactional(readOnly = true)
    public PortfolioListDto getPortfolioList(Long userId) {
        PortfolioListDto portfolioListDto = new PortfolioListDto();

        List<Portfolio> list = portfolioRepository.findAllByUserUserId(userId);

        if (list == null || list.isEmpty())
            throw new PortfolioNotFoundException();

        list.forEach(p -> {
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
        // TODO: 포트폴리오 조회할 때 마다 포트폴리오 budget, ret 필드 최신화
        // 수정전까진 현재 정보 기준으로 ror 계산
        // 수정후에는 현재가로 ret 갱신, ror 계산 후 save
        PortfolioResponseDto portfolioResponseDto = new PortfolioResponseDto();
        Portfolio portfolio = portfolioRepository.findById(pfid).orElseThrow(PortfolioNotFoundException::new);
        List<Holding> holdings = portfolio.getHoldings();

        if (holdings == null || holdings.isEmpty())
            return portfolioResponseDto;

        // stocks list
        holdings.forEach(h -> {
            Stock stock = h.getStock();
            // TODO: 현재가 추가 필요 - spring scheduler 서버로 갱신
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
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundExeption::new);
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
    public ResponseEntity<?> buyStock(Long pfid, String code, int quantity, int price) {
        Holding holding = holdingRepository.findHoldingByPortfolioIdAndStockCode(pfid, code).orElseThrow(HoldingNotFoundExeption::new);
        holding.additionalBuyHolding(quantity, price);
        // TODO: Holding 추가할 때 마다 포트폴리오 budget, ret 필드 최신화
        holdingRepository.save(holding);
        return ResponseEntity.ok().build();
    }
}
