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
import com.hrth.ustock.exception.*;
import com.hrth.ustock.repository.HoldingRepository;
import com.hrth.ustock.repository.PortfolioRepository;
import com.hrth.ustock.repository.StockRepository;
import com.hrth.ustock.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static com.hrth.ustock.service.StockServiceConst.REDIS_CURRENT_KEY;

@Slf4j
@Service
@RequiredArgsConstructor
public class PortfolioService {
    public static final int MAX_QUANTITY = 99_999;
    public static final long MAX_PRICE = 9_999_999_999L;

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

        // 현재가로 ret 갱신, ror 계산 후 save
        for (Holding h : holdings) {
            Map<String, String> redisMap = stockService.getCurrentChangeChangeRate(h.getStock().getCode());
            if (redisMap == null) {
                log.info("no current for stock: {}", h.getStock().getCode());
                continue;
            }
            int quantity = h.getQuantity();
            long average = h.getAverage();
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
        }

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

        Portfolio findPortfolio = portfolioRepository.findByNameAndUserUserId(portfolio.getName(), userId).orElse(null);
        if (findPortfolio != null) {
            throw new ExistPortfolioNameException();
        }

        portfolioRepository.save(portfolio);
    }

    @Transactional
    public void buyStock(Long pfId, String code, HoldingRequestDto holdingRequestDto) {
        Holding testHolding = holdingRepository.findHoldingByPortfolioIdAndStockCode(pfId, code).orElse(null);

        if (testHolding != null) {
            additionalBuyStock(pfId, code, holdingRequestDto);
            return;
        }

        int quantity = holdingRequestDto.getQuantity();
        long price = holdingRequestDto.getPrice();
        if (quantity > MAX_QUANTITY || price > MAX_PRICE) {
            throw new InputNotValidException();
        }

        Portfolio portfolio = portfolioRepository.findById(pfId).orElseThrow(PortfolioNotFoundException::new);
        Stock stock = stockRepository.findByCode(code).orElseThrow(StockNotFoundException::new);
        Holding holding = Holding.builder()
                .portfolio(portfolio)
                .stock(stock)
                .average(price)
                .quantity(quantity)
                .user(portfolio.getUser())
                .build();

        holdingRepository.save(holding);
    }

    // 10. 개별 종목 추가 매수
    @Transactional
    public void additionalBuyStock(Long pfId, String code, HoldingRequestDto holdingRequestDto) {

        Holding target = holdingRepository.findHoldingByPortfolioIdAndStockCode(pfId, code).orElseThrow(HoldingNotFoundException::new);

        long price = holdingRequestDto.getPrice();
        int quantity = holdingRequestDto.getQuantity();

        if (quantity > MAX_QUANTITY || price > MAX_PRICE) {
            throw new InputNotValidException();
        }

        target.additionalBuyHolding(quantity, price);
    }

    // 11. 개별 종목 수정
    @Transactional
    public void editHolding(Long pfId, String code, HoldingRequestDto holdingRequestDto) {

        Holding target = holdingRepository.findHoldingByPortfolioIdAndStockCode(pfId, code).orElseThrow(HoldingNotFoundException::new);

        int quantity = holdingRequestDto.getQuantity();
        long price = holdingRequestDto.getPrice();
        if (quantity > MAX_QUANTITY || price > MAX_PRICE) {
            throw new InputNotValidException();
        }
        target.updateHolding(quantity, price);
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

        if (list == null || list.isEmpty()) {
            portfolio.updatePortfolio(portfolioUpdateDto);
            return;
        }

        // budget = 원금+수익, principal = 갯수+평단가, ret = 갯수+현재가
        for (Holding h : list) {
            Map<String, String> redisMap = stockService.getCurrentChangeChangeRate(h.getStock().getCode());
            if (redisMap == null) {
                log.info("no current for stock: {}", h.getStock().getCode());
                continue;
            }
            int current = Integer.parseInt(redisMap.get(REDIS_CURRENT_KEY));

            long before = (long) h.getQuantity() * h.getAverage();
            long after = (long) h.getQuantity() * current;
            portfolioUpdateDto.setPrincipal(portfolioUpdateDto.getPrincipal() + before);
            portfolioUpdateDto.setRet(portfolioUpdateDto.getRet() + after - before);
        }

        portfolioUpdateDto.setBudget(portfolioUpdateDto.getPrincipal() + portfolioUpdateDto.getRet());
        portfolio.updatePortfolio(portfolioUpdateDto);
        portfolioRepository.save(portfolio);
    }
}
