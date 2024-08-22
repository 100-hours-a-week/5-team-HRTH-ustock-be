package com.hrth.ustock.service;

import com.hrth.ustock.dto.holding.HoldingEmbedDto;
import com.hrth.ustock.dto.holding.HoldingRequestDto;
import com.hrth.ustock.dto.portfolio.PortfolioListDto;
import com.hrth.ustock.dto.portfolio.PortfolioRequestDto;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PortfolioService {
    public static final String REDIS_CURRENT_KEY = "current";
    private final PortfolioRepository portfolioRepository;
    private final UserRepository userRepository;
    private final HoldingRepository holdingRepository;
    private final StockRepository stockRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final StockService stockService;

    @Transactional(readOnly = true)
    public PortfolioListDto getPortfolioList(Long userId) {

        PortfolioListDto portfolioListDto = new PortfolioListDto();
        List<Portfolio> list = portfolioRepository.findAllByUserUserId(userId);

        if (list == null || list.isEmpty())
            throw new PortfolioNotFoundException();

        list.forEach(p -> {
            refreshPortfolio(p);
            portfolioListDto.setBudget(portfolioListDto.getBudget() + p.getBudget());
            portfolioListDto.setPrincipal(portfolioListDto.getPrincipal() + p.getPrincipal());
            portfolioListDto.setRet(portfolioListDto.getRet() + p.getRet());
            portfolioListDto.getList().add(p.toPortfolioDto());
        });
        double ror = portfolioListDto.getPrincipal() == 0L ? 0.0 : (double) portfolioListDto.getRet() / portfolioListDto.getPrincipal() * 100;
        portfolioListDto.setRor(ror);

        return portfolioListDto;
    }

    @Transactional
    public PortfolioResponseDto getPortfolio(Long pfId) {

        PortfolioResponseDto portfolioResponseDto = new PortfolioResponseDto();
        Portfolio portfolio = portfolioRepository.findById(pfId).orElseThrow(PortfolioNotFoundException::new);
        List<Holding> holdings = portfolio.getHoldings();

        refreshPortfolio(portfolio);
        if (holdings == null || holdings.isEmpty()) {
            portfolioResponseDto.setName(portfolio.getName());
            return portfolioResponseDto;
        }

        // 현재가 반영전까진 현재 정보 기준으로 ror 계산
        // 현재가 반영후에는 현재가로 ret 갱신, ror 계산 후 save
        holdings.forEach(h -> {
            Map<String, String> redisMap =  stockService.cacheCurrentChangeChangeRate(h.getStock().getCode());

            int quantity = h.getQuantity();
            int average = h.getAverage();
            int current = Integer.parseInt(redisMap.get(REDIS_CURRENT_KEY));
            long ret = (long) quantity * current - (long) quantity * average;


            Stock stock = h.getStock();
            HoldingEmbedDto holdingEmbedDto = HoldingEmbedDto.builder()
                    .code(stock.getCode())
                    .name(stock.getName())
                    .logo(stock.getLogo())
                    .quantity(quantity)
                    .average(average)
                    .ror((quantity * average == 0) ? 0.0 : (double) ret / ((long) quantity * average) * 100)
                    .build();
            portfolioResponseDto.getStocks().add(holdingEmbedDto);
        });

        portfolioResponseDto.setName(portfolio.getName());
        portfolioResponseDto.setBudget(portfolio.getBudget());
        portfolioResponseDto.setPrincipal(portfolio.getPrincipal());
        portfolioResponseDto.setRet(portfolio.getRet());
        portfolioResponseDto.setRor((portfolio.getPrincipal() == 0L) ? 0.0 : (double) portfolio.getRet() / portfolio.getPrincipal() * 100);
        return portfolioResponseDto;
    }

    @Transactional
    public void addPortfolio(PortfolioRequestDto portfolioRequestDto, Long userId) {

        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        Portfolio portfolio = Portfolio.builder()
                .name(portfolioRequestDto.getName())
                .user(user)
                .budget(0L)
                .principal(0L)
                .ret(0L)
                .build();

        portfolioRepository.save(portfolio);
    }

    @Transactional
    public void buyStock(Long pfId, String code, HoldingRequestDto holdingRequestDto) {
        Holding testHolding = holdingRepository.findHoldingByPortfolioIdAndStockCode(pfId, code).orElse(null);

        if (testHolding != null) {
            additionalBuyStock(pfId, code, holdingRequestDto);
            return;
        }

        Portfolio portfolio = portfolioRepository.findById(pfId).orElseThrow(PortfolioNotFoundException::new);
        Stock stock = stockRepository.findByCode(code).orElseThrow(StockNotFoundException::new);
        Holding holding = Holding.builder()
                .portfolio(portfolio)
                .stock(stock)
                .average(holdingRequestDto.getPrice())
                .quantity(holdingRequestDto.getQuantity())
                .build();

        holdingRepository.save(holding);
    }

    // 10. 개별 종목 추가 매수
    @Transactional
    public void additionalBuyStock(Long pfId, String code, HoldingRequestDto holdingRequestDto) {

        Holding target = holdingRepository.findHoldingByPortfolioIdAndStockCode(pfId, code).orElseThrow(HoldingNotFoundException::new);

        target.additionalBuyHolding(holdingRequestDto.getQuantity(), holdingRequestDto.getPrice());
    }

    // 11. 개별 종목 수정
    @Transactional
    public void editHolding(Long pfId, String code, HoldingRequestDto holdingRequestDto) {

        Holding target = holdingRepository.findHoldingByPortfolioIdAndStockCode(pfId, code).orElseThrow(HoldingNotFoundException::new);

        target.updateHolding(holdingRequestDto.getQuantity(), holdingRequestDto.getPrice());
    }

    // 12. 개별 종목 삭제
    @Transactional
    public void deleteHolding(Long pfId, String code) {

        Portfolio portfolio = portfolioRepository.findById(pfId).orElseThrow(PortfolioNotFoundException::new);
      
        Holding target = holdingRepository.findHoldingByPortfolioIdAndStockCode(pfId, code).orElseThrow(HoldingNotFoundException::new);

        holdingRepository.delete(target);
        portfolio.getHoldings().remove(target);
    }

    // 13. 포트폴리오 삭제
    @Transactional
    public void deletePortfolio(Long pfId) {

        Portfolio portfolio = portfolioRepository.findById(pfId).orElseThrow(PortfolioNotFoundException::new);
        List<Holding> list = portfolio.getHoldings();

        // 보유 종목 없을시 바로 삭제, 아니면 보유종목 전부 삭제
        if (list != null && !list.isEmpty()) {
            holdingRepository.deleteAll(list);
        }

        portfolioRepository.delete(portfolio);
    }

    @Transactional
    protected void refreshPortfolio(Portfolio portfolio) {
        PortfolioUpdateDto portfolioUpdateDto = new PortfolioUpdateDto();
        List<Holding> list = portfolio.getHoldings();

        if (list == null || list.isEmpty()) return;

        // budget = 원금+수익, principal = 갯수+평단가, ret = 갯수+현재가
        list.forEach(h -> {
            Map<String, String> redisMap = stockService.cacheCurrentChangeChangeRate(h.getStock().getCode());
            int current = Integer.parseInt(redisMap.get(REDIS_CURRENT_KEY));

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
