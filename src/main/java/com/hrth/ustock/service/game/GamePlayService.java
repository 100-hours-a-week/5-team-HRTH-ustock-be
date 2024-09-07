package com.hrth.ustock.service.game;

import com.hrth.ustock.dto.game.redis.GameHoldingsInfo;
import com.hrth.ustock.dto.game.redis.GameUserInfo;
import com.hrth.ustock.dto.game.stock.GameStockInfoResponseDto;
import com.hrth.ustock.dto.game.stock.GameStocksRedisDto;
import com.hrth.ustock.entity.game.GameStockIndustry;
import com.hrth.ustock.entity.game.GameStockInfo;
import com.hrth.ustock.entity.game.PlayerType;
import com.hrth.ustock.entity.main.User;
import com.hrth.ustock.exception.domain.game.GameException;
import com.hrth.ustock.exception.domain.user.UserException;
import com.hrth.ustock.repository.UserRepository;
import com.hrth.ustock.repository.game.GameHintRepository;
import com.hrth.ustock.repository.game.GameNewsRepository;
import com.hrth.ustock.repository.game.GameStockInfoRepository;
import com.hrth.ustock.repository.game.GameStockYearlyRepository;
import com.hrth.ustock.util.RedisJsonManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.hrth.ustock.exception.domain.game.GameExceptionType.GAME_NOT_FOUND;
import static com.hrth.ustock.exception.domain.user.UserExceptionType.USER_NOT_FOUND;
import static com.hrth.ustock.service.game.GameInfoConst.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class GamePlayService {
    private final int STOCK_COUNT = 8;
    private final int USER_COUNT = 4;

    private final UserRepository userRepository;
    private final GameHintRepository gameHintRepository;
    private final GameNewsRepository gameNewsRepository;
    private final GameStockInfoRepository gameStockInfoRepository;
    private final GameStockYearlyRepository gameStockYearlyRepository;

    private final RedisTemplate<String, String> redisTemplate;
    private final RedisJsonManager redisJsonManager;


    public void startGame(Long userId, String nickname) {

        User user = userRepository.findById(userId).orElseThrow(() -> new UserException(USER_NOT_FOUND));


        List<GameStockInfo> stockInfoList = gameStockInfoRepository.findAll();
//        Collections.shuffle(stockInfoList);

        List<GameStocksRedisDto> selectedList = new ArrayList<>();
        for (int i = 0; i < STOCK_COUNT; i++) {
            GameStockInfo gameStockInfo = stockInfoList.get(i);

            selectedList.add(GameStocksRedisDto.builder()
                    .id(gameStockInfo.getId())
                    .stockName(makeFakeStockName(i, gameStockInfo.getIndustry()))
                    .build()
            );
        }

        List<GameUserInfo> userInfoList = new ArrayList<>();
        for (int i = 0; i < USER_COUNT; i++) {
            List<GameHoldingsInfo> holdings = new ArrayList<>();
            userInfoList.add(GameUserInfo.builder()
                    .ret(0)
                    .year(2014)
                    .change(0)
                    .changeRate(0.0)
                    .playerType(i == 0 ? PlayerType.USER : PlayerType.COM)
                    .budget(500000)
                    .holdings(holdings)
                    .build()
            );
        }

        String stockIdList = redisJsonManager.serializeList(selectedList);
        String userList = redisJsonManager.serializeList(userInfoList);
        String gameKey = GAME_KEY + user.getUserId();
        redisTemplate.opsForHash().put(gameKey, STOCKS_KEY, stockIdList);
        redisTemplate.opsForHash().put(gameKey, NICKNAME_KEY, nickname);
        redisTemplate.opsForHash().put(gameKey, USER_KEY, userList);
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

    public List<GameStockInfoResponseDto> showStockList(int year, long userId) {
        String gameStocksJson = (String) redisTemplate.opsForHash().get(GAME_KEY + userId, STOCKS_KEY);

        if (gameStocksJson == null)
            throw new GameException(GAME_NOT_FOUND);

        List<GameStocksRedisDto> gameStocks = redisJsonManager.deserializeList(gameStocksJson, GameStocksRedisDto[].class);

        List<GameStockInfoResponseDto> gameInfoList = new ArrayList<>();
        for (GameStocksRedisDto gameStock : gameStocks) {
            Long stockId = gameStock.getId();

            GameStockInfoResponseDto gameStockInfo = GameStockInfoResponseDto.builder()
                    .stockId(stockId)
                    .name(gameStock.getStockName())
                    .build();

            Integer prev = gameStockYearlyRepository.findPriceByGameStockInfoIdAndYear(stockId, year - 1);
            gameStockInfo.setPrev(prev);

            Integer current = gameStockYearlyRepository.findPriceByGameStockInfoIdAndYear(stockId, year);
            gameStockInfo.setCurrent(current);

            if (prev != null) {
                int change = current - prev;
                gameStockInfo.setChange(change);

                double changeRate = (double) change / prev * 100;
                gameStockInfo.setChangeRate(Math.round(changeRate * 100.0) / 100.0);
            }

            gameInfoList.add(gameStockInfo);
        }

        return gameInfoList;
    }
}
