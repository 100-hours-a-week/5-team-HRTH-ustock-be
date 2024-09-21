package com.hrth.ustock.service.game;

import com.hrth.ustock.dto.game.ai.GameAiSelectDto;
import com.hrth.ustock.dto.game.ai.GameAiStockDto;
import com.hrth.ustock.dto.game.hint.GameHintResponseDto;
import com.hrth.ustock.dto.game.interim.GameInterimResponseDto;
import com.hrth.ustock.dto.game.redis.GameHintCheckDto;
import com.hrth.ustock.dto.game.redis.GameHoldingsInfoDto;
import com.hrth.ustock.dto.game.redis.GameStocksRedisDto;
import com.hrth.ustock.dto.game.redis.GameUserInfoDto;
import com.hrth.ustock.dto.game.result.GameResultChartDto;
import com.hrth.ustock.dto.game.result.GameResultNewsDto;
import com.hrth.ustock.dto.game.result.GameResultResponseDto;
import com.hrth.ustock.dto.game.result.GameResultStockDto;
import com.hrth.ustock.dto.game.stock.GameStockInfoResponseDto;
import com.hrth.ustock.dto.game.stock.GameTradeRequestDto;
import com.hrth.ustock.dto.game.user.GameUserResponseDto;
import com.hrth.ustock.entity.game.*;
import com.hrth.ustock.entity.main.User;
import com.hrth.ustock.exception.domain.game.GameException;
import com.hrth.ustock.exception.domain.user.UserException;
import com.hrth.ustock.repository.UserRepository;
import com.hrth.ustock.repository.game.GameHintRepository;
import com.hrth.ustock.repository.game.GameResultRepository;
import com.hrth.ustock.repository.game.GameStockInfoRepository;
import com.hrth.ustock.repository.game.GameStockYearlyRepository;
import com.hrth.ustock.util.RedisJsonManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.hrth.ustock.entity.game.GameActing.BUY;
import static com.hrth.ustock.entity.game.GameActing.SELL;
import static com.hrth.ustock.entity.game.PlayerType.COM;
import static com.hrth.ustock.entity.game.PlayerType.USER;
import static com.hrth.ustock.exception.domain.game.GameExceptionType.*;
import static com.hrth.ustock.exception.domain.user.UserExceptionType.USER_NOT_FOUND;
import static com.hrth.ustock.service.game.GameInfoConst.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class GamePlayService {
    private static final int USER_INDEX = 0;
    private static final int START_BUDGET = 500_000;
    private static final int HINT_ONE_PRICE = 100_000;
    private static final int HINT_TWO_PRICE = 300_000;
    private static final int HINT_THREE_PRICE = 500_000;

    private final UserRepository userRepository;
    private final GameStockInfoRepository gameStockInfoRepository;
    private final GameStockYearlyRepository gameStockYearlyRepository;
    private final GameHintRepository gameHintRepository;
    private final GameResultRepository gameResultRepository;

    private final GameAiService gameAiService;

    private final RedisTemplate<String, String> redisTemplate;
    private final RedisJsonManager redisJsonManager;


    public List<GameStockInfoResponseDto> startGame(Long userId, String nickname) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserException(USER_NOT_FOUND));

        List<GameStockInfo> stockInfoList = gameStockInfoRepository.findAll();
        // TODO: 이후에 실제 데이터 추가하고 주석 해제
//        Collections.shuffle(stockInfoList);
//        stockInfoList.sort(Comparator.comparingLong(GameStockInfo::getId));

        List<GameStocksRedisDto> selectedList = new ArrayList<>();
        int stockCount = 8;
        for (int i = 0; i < stockCount; i++) {
            GameStockInfo gameStockInfo = stockInfoList.get(i);

            selectedList.add(GameStocksRedisDto.builder()
                    .id(gameStockInfo.getId())
                    .stockName(makeFakeStockName(i, gameStockInfo.getIndustry()))
                    .realName(gameStockInfo.getStockName())
                    .build()
            );
        }

        List<GameUserInfoDto> userInfoList = new ArrayList<>();
        int userCount = 4;
        for (int i = 0; i < userCount; i++) {
            ArrayList<GameHoldingsInfoDto> holdings = new ArrayList<>();

            userInfoList.add(GameUserInfoDto.builder()
                    .playerType(i == USER_INDEX ? USER : COM)
                    .nickname(i == USER_INDEX ? nickname : "COM" + i)
                    .hintCheck(new GameHintCheckDto())
                    .budget(START_BUDGET)
                    .prev(START_BUDGET)
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

        tradePlayer(userId, year);

        return showStockList(userId);
    }

    public List<GameUserResponseDto> getGamePlayerList(long userId) {
        List<GameUserInfoDto> userInfoList = getUserInfoList(userId);
        List<GameUserResponseDto> playerList = new ArrayList<>();

        for (GameUserInfoDto userInfo : userInfoList) {
            if (userInfo.getPlayerType() == COM) {
                long total = userInfo.getBudget();

                for (GameHoldingsInfoDto holding : userInfo.getHoldings()) {
                    total += ((long) holding.getPrice() * holding.getQuantity());
                }

                long prev = userInfo.getPrev();
                playerList.add(GameUserResponseDto.builder()
                        .budget(userInfo.getBudget())
                        .nickname(userInfo.getNickname())
                        .total(total)
                        .changeFromLast(total - prev)
                        .changeRateFromLast(calcRate(prev, total))
                        .changeFromStart(total - START_BUDGET)
                        .changeRateFromStart(calcRate(START_BUDGET, total))
                        .build()
                );
            }
        }

        return playerList;
    }

    public GameUserResponseDto getUserInfo(long userId) {
        List<GameUserInfoDto> userInfoList = getUserInfoList(userId);
        GameUserInfoDto userInfo = userInfoList.get(USER_INDEX);
        List<GameHoldingsInfoDto> holdingsInfo = userInfo.getHoldings();

        long total = userInfo.getBudget();
        for (GameHoldingsInfoDto holding : holdingsInfo) {
            total += (long) holding.getPrice() * holding.getQuantity();
        }
        long prev = userInfo.getPrev();

        return GameUserResponseDto.builder()
                .nickname(userInfo.getNickname())
                .total(total)
                .budget(userInfo.getBudget())
                .changeFromLast(total - prev)
                .changeRateFromLast(calcRate(prev, total))
                .changeFromStart(total - START_BUDGET)
                .changeRateFromStart(calcRate(START_BUDGET, total))
                .holdingList(holdingsInfo)
                .build();
    }

    public void tradeHolding(long userId, GameTradeRequestDto requestDto, int playerId) {
        long stockId = requestDto.getStockId();
        int quantity = requestDto.getQuantity();
        GameActing act = requestDto.getActing();

        switch (act) {
            case BUY -> buyHolding(userId, stockId, quantity, playerId);
            case SELL -> sellHolding(userId, stockId, quantity, playerId);
        }
    }

    public GameHintResponseDto getSingleHint(long userId, long stockId, HintLevel hintLevel) {
        int year = getGameYear(userId);
        List<GameUserInfoDto> userInfoList = getUserInfoList(userId);
        GameUserInfoDto userInfo = userInfoList.get(USER_INDEX);

        boolean flag = switch (hintLevel) {
            case ONE -> userInfo.getHintCheck().getLevelOneId() != 0;
            case TWO -> userInfo.getHintCheck().getLevelTwoId() != 0;
            case THREE -> userInfo.getHintCheck().getLevelThreeId() != 0;
        };

        if (flag) {
            throw new GameException(HINT_ALREADY_USED);
        }

        GameStockYearly yearInfo = gameStockYearlyRepository.findByGameStockInfoIdAndYear(stockId, year)
                .orElseThrow(() -> new GameException(YEAR_INFO_NOT_FOUND));

        GameHintResponseDto hint = gameHintRepository.findByGameStockYearlyIdAndLevel(yearInfo.getId(), hintLevel)
                .orElseThrow(() -> new GameException(HINT_NOT_FOUND))
                .toDto();

        long budget = userInfo.getBudget();
        int price = switch (hintLevel) {
            case ONE -> {
                userInfo.getHintCheck().setLevelOneId(stockId);
                yield HINT_ONE_PRICE;
            }
            case TWO -> {
                userInfo.getHintCheck().setLevelTwoId(stockId);
                yield HINT_TWO_PRICE;
            }
            case THREE -> {
                userInfo.getHintCheck().setLevelThreeId(stockId);
                yield HINT_THREE_PRICE;
            }
        };

        reduceHintBudget(budget, userInfo, price);

        String json = redisJsonManager.serializeList(userInfoList);
        redisTemplate.opsForHash().put(GAME_KEY + userId, USER_KEY, json);

        return hint;
    }

    private void reduceHintBudget(long budget, GameUserInfoDto userInfo, long price) {
        if (budget < price) {
            throw new GameException(NOT_ENOUGH_BUDGET);
        }
        userInfo.setBudget(budget - price);
    }

    public GameInterimResponseDto getUserInterim(long userId) {
        int year = getGameYear(userId);
        if (2023 <= year) {
            throw new GameException(GAME_END);
        }

        List<GameUserInfoDto> userInfoList = getUserInfoList(userId);

        int lastYear = year;
        userInfoList.forEach(userInfo -> {
            GameHintCheckDto hintCheck = userInfo.getHintCheck();
            List<GameHoldingsInfoDto> holdingsInfo = userInfo.getHoldings();

            long prev = userInfo.getBudget();
            for (GameHoldingsInfoDto holding : holdingsInfo) {
                prev += (long) holding.getPrice() * holding.getQuantity();
                int price = getStockPrice(holding.getStockId(), lastYear);
                holding.setPrice(price);
                holding.setProfitRate(calcRate(holding.getAverage(), price));
            }
            userInfo.setPrev(prev);
            hintCheck.setLevelOneId(0);
            hintCheck.setLevelTwoId(0);
            hintCheck.setLevelThreeId(0);
        });

        year++;
        redisTemplate.opsForHash().put(GAME_KEY + userId, YEAR_KEY, String.valueOf(year));

        String json = redisJsonManager.serializeList(userInfoList);
        redisTemplate.opsForHash().put(GAME_KEY + userId, USER_KEY, json);

        tradePlayer(userId, year);

        return GameInterimResponseDto.builder()
                .year(year)
                .stockList(showStockList(userId))
                .userInfo(getUserInfo(userId))
                .build();
    }

    public List<GameResultResponseDto> getGameResultList(long userId) {
        List<GameUserInfoDto> userInfoList = getUserInfoList(userId);
        List<GameResultResponseDto> gameResultList = new ArrayList<>();

        for (GameUserInfoDto userInfo : userInfoList) {
            List<GameHoldingsInfoDto> holdingsInfo = userInfo.getHoldings();

            long total = userInfo.getBudget();
            for (GameHoldingsInfoDto holding : holdingsInfo) {
                total += (long) holding.getPrice() * holding.getQuantity();
            }

            gameResultList.add(
                    GameResultResponseDto.builder()
                            .nickname(userInfo.getNickname())
                            .playerType(userInfo.getPlayerType())
                            .total(total)
                            .profitRate(calcRate(START_BUDGET, total))
                            .build()
            );
        }
        return gameResultList;
    }

    public List<GameResultStockDto> getGameResultStock(long userId) {
        List<GameResultStockDto> gameResultStockDtoList = new ArrayList<>();

        List<GameStocksRedisDto> gameStocks = getGameStocks(userId);
        List<Long> gameStockIdList = gameStocks.stream()
                .map(GameStocksRedisDto::getId)
                .toList();
        List<GameStockYearly> gameStockYearlyList = gameStockYearlyRepository.findAllByGameStockInfoIdIn(gameStockIdList);

        for (GameStocksRedisDto stock : gameStocks) {
            long stockId = stock.getId();
            List<GameStockYearly> thisGameStockYearlyList = gameStockYearlyList.stream()
                    .filter(yearly -> yearly.getGameStockInfo().getId() == stockId)
                    .toList();

            List<GameResultChartDto> chartList = new ArrayList<>();
            List<GameResultNewsDto> newsList = new ArrayList<>();
            for (GameStockYearly gameStockYearly : thisGameStockYearlyList) {
                chartList.add(
                        GameResultChartDto.builder()
                                .x(gameStockYearly.getDate())
                                .y(gameStockYearly.getPrice())
                                .build()
                );
                newsList.add(gameStockYearly.getGameNews().toDto());
            }

            gameResultStockDtoList.add(
                    GameResultStockDto.builder()
                            .stockId(stockId)
                            .fakeName(stock.getStockName())
                            .realName(stock.getRealName())
                            .chart(chartList)
                            .news(newsList)
                            .build()
            );
        }
        return gameResultStockDtoList;
    }

    public void saveRankInfo(long userId) {
        GameUserResponseDto userInfo = getUserInfo(userId);
        gameResultRepository.save(GameResult.builder()
                .budget(userInfo.getTotal())
                .nickname(userInfo.getNickname())
                .build()
        );
        redisTemplate.delete(GAME_KEY + userId);
    }

    private void tradePlayer(long userId, int year) {
        List<GameUserInfoDto> userInfoList = getUserInfoList(userId);
        Map<String, GameAiSelectDto> aiSelectResult = gameAiService.getAiSelectResult(
                year, userInfoList, showStockList(userId)
        );

        for (int i = 1; i <= 3; i++) {
            String aiName = userInfoList.get(i).getNickname();
            GameAiSelectDto gameAiSelectDto = aiSelectResult.get(aiName);

            for (GameAiStockDto gameAiStockDto : gameAiSelectDto.getSell()) {
                GameTradeRequestDto requestDto = GameTradeRequestDto.builder()
                        .acting(SELL)
                        .stockId(gameAiStockDto.getId())
                        .quantity(gameAiStockDto.getQuantity())
                        .build();
                tradeHolding(userId, requestDto, i);
            }

            for (GameAiStockDto gameAiStockDto : gameAiSelectDto.getBuy()) {
                GameTradeRequestDto requestDto = GameTradeRequestDto.builder()
                        .acting(BUY)
                        .stockId(gameAiStockDto.getId())
                        .quantity(gameAiStockDto.getQuantity())
                        .build();
                tradeHolding(userId, requestDto, i);
            }
        }
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

    private List<GameStockInfoResponseDto> showStockList(long userId) {
        int year = getGameYear(userId);

        List<GameStocksRedisDto> gameStocks = getGameStocks(userId);

        List<GameStockInfoResponseDto> gameInfoList = new ArrayList<>();

        List<Long> stockIds = gameStocks.stream()
                .map(GameStocksRedisDto::getId)
                .toList();
        List<Integer> prevList = getPriceList(stockIds, year - 1);
        List<Integer> currentList = getPriceList(stockIds, year);

        for (int i = 0; i < stockIds.size(); i++) {
            GameStocksRedisDto gameStock = gameStocks.get(i);
            int prev = prevList.isEmpty() ? 0 : prevList.get(i);

            Long stockId = gameStock.getId();
            GameStockInfoResponseDto gameStockInfo = GameStockInfoResponseDto.builder()
                    .stockId(stockId)
                    .name(gameStock.getStockName())
                    .build();

            gameStockInfo.setPrev(prev);

            int current = currentList.get(i);
            gameStockInfo.setCurrent(current);

            if (prev != 0) {
                int change = current - prev;
                gameStockInfo.setChange(change);
                gameStockInfo.setChangeRate(calcRate(prev, current));
            }
            gameInfoList.add(gameStockInfo);
        }
        return gameInfoList;
    }

    private List<Integer> getPriceList(List<Long> stockIds, int year) {
        return gameStockYearlyRepository.findPriceListByGameStockInfoIdAndYear(stockIds, year)
                .stream()
                .map(GameStockYearly::getPrice)
                .toList();
    }

    private void buyHolding(long userId, long stockId, int quantity, int playerId) {
        int year = getGameYear(userId);

        List<GameUserInfoDto> userInfoList = getUserInfoList(userId);

        GameUserInfoDto userInfo = userInfoList.get(playerId);
        List<GameHoldingsInfoDto> holdingsList = userInfo.getHoldings();
        int price = getStockPrice(stockId, year);

        long totalPrice = (long) price * quantity;

        if (totalPrice > userInfo.getBudget()) {
            // TODO: AI 로직 수정할때까지만 임시로 추가한 return state
            if (playerId != USER_INDEX) return;
            throw new GameException(NOT_ENOUGH_BUDGET);
        }

        GameHoldingsInfoDto holding = holdingsList.stream()
                .filter(dto -> dto.getStockId() == stockId)
                .findFirst()
                .orElse(null);

        if (holding != null) {
            holding.setAverage(calcAverage(holding, quantity, price));
            holding.setProfitRate(calcRate(holding.getAverage(), price));
            holding.setQuantity(holding.getQuantity() + quantity);
        } else {
            String stockName = getGameStocks(userId).stream()
                    .filter(stock -> stock.getId() == stockId)
                    .findFirst()
                    .orElseThrow(() -> new GameException(STOCK_NOT_FOUND))
                    .getStockName();
            holding = GameHoldingsInfoDto.builder()
                    .stockName(stockName)
                    .stockId(stockId)
                    .average(price)
                    .price(price)
                    .profitRate(0.0)
                    .quantity(quantity)
                    .build();

            holdingsList.add(holding);
        }

        userInfo.setBudget(userInfo.getBudget() - totalPrice);
        String json = redisJsonManager.serializeList(userInfoList);
        redisTemplate.opsForHash().put(GAME_KEY + userId, USER_KEY, json);
    }

    private void sellHolding(long userId, long stockId, int quantity, int playerId) {
        int year = getGameYear(userId);

        List<GameUserInfoDto> userInfoList = getUserInfoList(userId);

        GameUserInfoDto userInfo = userInfoList.get(playerId);
        List<GameHoldingsInfoDto> holdingsList = userInfo.getHoldings();
        int price = getStockPrice(stockId, year);

        GameHoldingsInfoDto holding = holdingsList.stream()
                .filter(dto -> dto.getStockId() == stockId)
                .findFirst()
                .orElseThrow(() -> new GameException(NO_HOLDING_STOCK));

        if (holding.getQuantity() < quantity) {
            // TODO: AI 로직 수정할때까지만 임시로 추가한 return state
            if (playerId != USER_INDEX) return;
            throw new GameException(NOT_ENOUGH_QUANTITY);
        }

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

    private long calcAverage(GameHoldingsInfoDto holding, int quantity, int price) {
        long total = holding.getAverage() * holding.getQuantity();
        long totalPrice = (long) quantity * price;
        total += totalPrice;
        total /= (holding.getQuantity() + quantity);

        return total;
    }

    private List<GameUserInfoDto> getUserInfoList(long userId) {
        String redisResult = (String) redisTemplate.opsForHash().get(GAME_KEY + userId, USER_KEY);

        if (redisResult == null)
            throw new GameException(GAME_NOT_FOUND);
        return redisJsonManager.deserializeList(redisResult, GameUserInfoDto[].class);
    }

    private double calcRate(long prev, long current) {
        if (prev == 0 || current == prev) return 0.0;

        double rate = (double) (current - prev) / prev * 100.0;
        return Math.round(rate * 100.0) / 100.0;
    }

    public void deleteRedis(long userId) {
        redisTemplate.delete(GAME_KEY + userId);
    }
}