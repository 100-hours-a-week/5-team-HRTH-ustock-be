package com.hrth.ustock.service.game;

import com.hrth.ustock.dto.game.hint.GameHintResponseDto;
import com.hrth.ustock.dto.game.redis.GameHoldingsInfo;
import com.hrth.ustock.dto.game.redis.GamePlayerInfoDto;
import com.hrth.ustock.dto.game.result.GamePlayerResponseDto;
import com.hrth.ustock.dto.game.stock.GameStockInfoResponseDto;
import com.hrth.ustock.dto.game.stock.GameStocksRedisDto;
import com.hrth.ustock.dto.game.stock.GameTradeRequestDto;
import com.hrth.ustock.entity.game.*;
import com.hrth.ustock.entity.main.User;
import com.hrth.ustock.exception.domain.game.GameException;
import com.hrth.ustock.exception.domain.user.UserException;
import com.hrth.ustock.repository.UserRepository;
import com.hrth.ustock.repository.game.GameHintRepository;
import com.hrth.ustock.repository.game.GameStockInfoRepository;
import com.hrth.ustock.repository.game.GameStockYearlyRepository;
import com.hrth.ustock.util.RedisJsonManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.hrth.ustock.exception.domain.game.GameExceptionType.*;
import static com.hrth.ustock.exception.domain.user.UserExceptionType.USER_NOT_FOUND;
import static com.hrth.ustock.service.game.GameInfoConst.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class GamePlayService {
    public static final long START_BUDGET = 500_000L;

    private final UserRepository userRepository;
    private final GameStockInfoRepository gameStockInfoRepository;
    private final GameStockYearlyRepository gameStockYearlyRepository;
    private final GameHintRepository gameHintRepository;

    private final RedisTemplate<String, String> redisTemplate;
    private final RedisJsonManager redisJsonManager;


    public void startGame(Long userId, String nickname) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserException(USER_NOT_FOUND));

        List<GameStockInfo> stockInfoList = gameStockInfoRepository.findAll();
        // TODO: 이후에 실제 데이터 추가하고 주석 해제
//        Collections.shuffle(stockInfoList);

        List<GameStocksRedisDto> selectedList = new ArrayList<>();
        int stockCount = 8;
        for (int i = 0; i < stockCount; i++) {
            GameStockInfo gameStockInfo = stockInfoList.get(i);

            selectedList.add(GameStocksRedisDto.builder()
                    .id(gameStockInfo.getId())
                    .stockName(makeFakeStockName(i, gameStockInfo.getIndustry()))
                    .build()
            );
        }

        List<GamePlayerInfoDto> userInfoList = new ArrayList<>();
        int userCount = 4;
        for (int i = 0; i < userCount; i++) {
            ArrayList<GameHoldingsInfo> holdings = new ArrayList<>();

            userInfoList.add(GamePlayerInfoDto.builder()
                    .playerType(i == 0 ? PlayerType.USER : PlayerType.COM)
                    .nickname(i == 0 ? nickname : "COM")
                    .budget(START_BUDGET)
                    .holdings(holdings)
                    .build()
            );
        }

        String stockIdList = redisJsonManager.serializeList(selectedList);
        String userList = redisJsonManager.serializeList(userInfoList);
        String gameKey = GAME_KEY + user.getUserId();
        int year = 2014;

        redisTemplate.opsForHash().put(gameKey, STOCKS_KEY, stockIdList);
        redisTemplate.opsForHash().put(gameKey, NICKNAME_KEY, nickname);
        redisTemplate.opsForHash().put(gameKey, YEAR_KEY, String.valueOf(year));
        redisTemplate.opsForHash().put(gameKey, USER_KEY, userList);
    }

    public List<GameStockInfoResponseDto> showStockList(long userId) {
        int year = getGameYear(userId);

        List<GameStocksRedisDto> gameStocks = getGameStocks(userId);
        List<GameStockInfoResponseDto> gameInfoList = new ArrayList<>();
        for (GameStocksRedisDto gameStock : gameStocks) {
            Long stockId = gameStock.getId();

            GameStockInfoResponseDto gameStockInfo = GameStockInfoResponseDto.builder()
                    .stockId(stockId)
                    .name(gameStock.getStockName())
                    .build();

            int prev = gameStockYearlyRepository.findPriceByGameStockInfoIdAndYear(stockId, year - 1)
                    .orElse(0);
            gameStockInfo.setPrev(prev);

            int current = getStockPrice(stockId, year);
            gameStockInfo.setCurrent(current);

            int change = prev != 0 ? current - prev : 0;
            gameStockInfo.setChange(change);

            gameStockInfo.setChangeRate(calcChangeRate(prev, current));

            gameInfoList.add(gameStockInfo);
        }

        return gameInfoList;
    }

    public List<GamePlayerInfoDto> getGameUserList(long userId) {
        return getUserInfoList(userId);
    }

    public GamePlayerResponseDto getPlayerInfo(long userId) {
        List<GamePlayerInfoDto> playerInfoList = getUserInfoList(userId);
        GamePlayerInfoDto userInfo = playerInfoList.get(0);
        List<GameHoldingsInfo> holdingsInfo = userInfo.getHoldings();

        long total = userInfo.getBudget();
        for (GameHoldingsInfo holding : holdingsInfo) {
            total += (long) holding.getPrice() * holding.getQuantity();
        }

        return GamePlayerResponseDto.builder()
                .nickname(userInfo.getNickname())
                .total(total)
                .budget(userInfo.getBudget())
                .changeFromLast(total - userInfo.getPrev())
                .changeRateFromLast(calcChangeRate(userInfo.getPrev(), total))
                .changeFromStart(total - START_BUDGET)
                .changeRateFromStart(calcChangeRate(START_BUDGET, total))
                .holdingList(holdingsInfo)
                .build();
    }

    public List<GameHoldingsInfo> showHoldingsList(long userId) {
        return getUserInfoList(userId).get(0).getHoldings();
    }

    public void tradeHolding(long userId, GameTradeRequestDto tradeRequestDto) {
        long stockId = tradeRequestDto.getStockId();
        int quantity = tradeRequestDto.getQuantity();
        GameActing act = tradeRequestDto.getActing();

        switch (act) {
            case BUY -> buyHolding(userId, stockId, quantity);
            case SELL -> sellHolding(userId, stockId, quantity);
        }
    }

    public GameHintResponseDto getSingleHint(long userId, long stockId, HintLevel hintLevel) {
        int year = getGameYear(userId);
        GameStockYearly yearInfo = gameStockYearlyRepository.findByGameStockInfoIdAndYear(stockId, year)
                .orElseThrow(() -> new GameException(YEAR_INFO_NOT_FOUND));

        return gameHintRepository.findByGameStockYearlyIdAndLevel(yearInfo.getId(), hintLevel)
                .map(GameHint::toDto)
                .orElseThrow(() -> new GameException(HINT_NOT_FOUND));
    }

    public GamePlayerResponseDto getPlayerInterim(long userId) {
        int year = getGameYear(userId);

        if (year == 2023) {
            // TODO: 2023년에 interim 요청시 게임 종료
        }

        List<GamePlayerInfoDto> userInfoList = getUserInfoList(userId);
        GamePlayerInfoDto userInfo = userInfoList.get(0);

        List<GameHoldingsInfo> holdingsInfo = userInfo.getHoldings();

        for (GameHoldingsInfo holding : holdingsInfo) {
            int price = getStockPrice(holding.getStockId(), year);
            holding.setPrice(price);
            holding.setRor(calcChangeRate(holding.getAverage(), price));
        }

        year++;
        redisTemplate.opsForHash().put(GAME_KEY + userId, YEAR_KEY, String.valueOf(year));

        String json = redisJsonManager.serializeList(userInfoList);
        redisTemplate.opsForHash().put(GAME_KEY + userId, USER_KEY, json);

        return getPlayerInfo(userId);
    }

    private String makeFakeStockName(int idx, GameStockIndustry industry) {
        char alphabet = (char) ('A' + idx);

        return switch (industry) {
            case GAME -> alphabet + " 게임";
            case AIR -> alphabet + " 항공";
            case IT -> alphabet + " IT";
            case BIO -> alphabet + " 바이오";
            case ENTER -> alphabet + " 엔터";
            case ELEC -> alphabet + " 전자";
            case CAR -> alphabet + " 자동차";
            case CHEM -> alphabet + " 화학";
            case FOOD -> alphabet + " 식품";
            case BANK -> alphabet + " 금융";
        };
    }

    private void buyHolding(long userId, long stockId, int quantity) {
        int year = getGameYear(userId);

        List<GamePlayerInfoDto> userInfoList = getUserInfoList(userId);

        GamePlayerInfoDto userInfo = userInfoList.get(0);
        List<GameHoldingsInfo> holdingsList = userInfo.getHoldings();
        int price = getStockPrice(stockId, year);

        long totalPrice = (long) price * quantity;

        if (totalPrice > userInfo.getBudget())
            throw new GameException(NOT_ENOUGH_BUDGET);

        GameHoldingsInfo holding = holdingsList.stream()
                .filter(dto -> dto.getStockId() == stockId)
                .findFirst()
                .orElse(null);

        if (holding != null) {
            holding.setAverage(calcAverage(holding, quantity, price));
            holding.setQuantity(holding.getQuantity() + quantity);
        } else {
            String stockName = getGameStocks(userId).stream()
                    .filter(stock -> stock.getId() == stockId)
                    .findFirst()
                    .orElseThrow(() -> new GameException(STOCK_NOT_FOUND))
                    .getStockName();

            holding = GameHoldingsInfo.builder()
                    .stockName(stockName)
                    .stockId(stockId)
                    .average(price)
                    .price(price)
                    .quantity(quantity)
                    .build();

            holdingsList.add(holding);
        }

        userInfo.setBudget(userInfo.getBudget() - totalPrice);
        String json = redisJsonManager.serializeList(userInfoList);
        redisTemplate.opsForHash().put(GAME_KEY + userId, USER_KEY, json);
    }

    private void sellHolding(long userId, long stockId, int quantity) {
        int year = getGameYear(userId);

        List<GamePlayerInfoDto> userInfoList = getUserInfoList(userId);

        GamePlayerInfoDto userInfo = userInfoList.get(0);
        List<GameHoldingsInfo> holdingsList = userInfo.getHoldings();
        int price = getStockPrice(stockId, year);

        GameHoldingsInfo holding = holdingsList.stream()
                .filter(dto -> dto.getStockId() == stockId)
                .findFirst()
                .orElseThrow(() -> new GameException(NO_HOLDING_STOCK));

        if (holding.getQuantity() < quantity)
            throw new GameException(NOT_ENOUGH_QUANTITY);

        long totalPrice = (long) price * quantity;
        holding.setQuantity(holding.getQuantity() - quantity);
        userInfo.setBudget(userInfo.getBudget() + totalPrice);

        if (holding.getQuantity() == 0)
            holdingsList.remove(holding);

        String json = redisJsonManager.serializeList(userInfoList);
        redisTemplate.opsForHash().put(GAME_KEY + userId, USER_KEY, json);
    }

    private int getStockPrice(long stockId, int year) {
        return gameStockYearlyRepository.findPriceByGameStockInfoIdAndYear(stockId, year)
                .orElseThrow(() -> new GameException(STOCK_NOT_FOUND));
    }

    private List<GameStocksRedisDto> getGameStocks(long userId) {
        String gameStocksJson = (String) redisTemplate.opsForHash().get(GAME_KEY + userId, STOCKS_KEY);

        if (gameStocksJson == null)
            throw new GameException(STOCK_NOT_FOUND);

        return redisJsonManager.deserializeList(gameStocksJson, GameStocksRedisDto[].class);
    }

    private Integer getGameYear(long userId) {
        String year = (String) redisTemplate.opsForHash().get(GAME_KEY + userId, YEAR_KEY);

        if (year == null)
            throw new GameException(GAME_NOT_FOUND);

        return Integer.parseInt(year);
    }

    private long calcAverage(GameHoldingsInfo holding, int quantity, int price) {
        long total = holding.getAverage() * holding.getQuantity();
        long totalPrice = (long) quantity * price;
        total += totalPrice;
        total /= (holding.getQuantity() + quantity);

        return total;
    }

    private List<GamePlayerInfoDto> getUserInfoList(long userId) {
        String redisResult = (String) redisTemplate.opsForHash().get(GAME_KEY + userId, USER_KEY);

        if (redisResult == null)
            throw new GameException(GAME_NOT_FOUND);

        return redisJsonManager.deserializeList(redisResult, GamePlayerInfoDto[].class);
    }

    private double calcChangeRate(long prev, long current) {
        if (prev == 0 || current == prev) return 0.0;

        double rate = (double) (current - prev) / prev * 100.0;
        return Math.round(rate * 100.0) / 100.0;
    }

    public void deleteRedis(long userId) {
        redisTemplate.delete(GAME_KEY + userId);
    }
}