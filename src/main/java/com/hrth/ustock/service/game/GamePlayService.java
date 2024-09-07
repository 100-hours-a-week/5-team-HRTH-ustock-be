package com.hrth.ustock.service.game;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrth.ustock.dto.game.stock.GameStockInfoResponseDto;
import com.hrth.ustock.dto.game.stock.GameStocksRedisDto;
import com.hrth.ustock.entity.game.GameStockIndustry;
import com.hrth.ustock.entity.game.GameStockInfo;
import com.hrth.ustock.entity.main.User;
import com.hrth.ustock.exception.domain.game.GameException;
import com.hrth.ustock.exception.domain.user.UserException;
import com.hrth.ustock.exception.redis.RedisException;
import com.hrth.ustock.repository.UserRepository;
import com.hrth.ustock.repository.game.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.hrth.ustock.exception.domain.game.GameExceptionType.GAME_NOT_FOUND;
import static com.hrth.ustock.exception.domain.user.UserExceptionType.USER_NOT_FOUND;
import static com.hrth.ustock.exception.redis.RedisExceptionType.DESERIALIZE_FAILED;
import static com.hrth.ustock.exception.redis.RedisExceptionType.SERIALIZE_FAILED;

@Slf4j
@Service
@RequiredArgsConstructor
public class GamePlayService {
    private final int STOCK_COUNT = 8;

    private final UserRepository userRepository;
    private final GameHintRepository gameHintRepository;
    private final GameNewsRepository gameNewsRepository;
    private final GameStockInfoRepository gameStockInfoRepository;
    private final GameStockYearlyRepository gameStockYearlyRepository;

    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, String> redisTemplate;

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

        String stockIdList = serializeStocks(selectedList);
    }

    private String serializeStocks(List<GameStocksRedisDto> selectedList) {
        try {
            return objectMapper.writeValueAsString(selectedList);
        } catch (JsonProcessingException e) {
            throw new RedisException(SERIALIZE_FAILED);
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

    public List<GameStockInfoResponseDto> showStockList(int year, long gameId) {
        String gameStocksJson = redisTemplate.opsForValue().get("game_stocks_" + gameId);

        if (gameStocksJson == null)
            throw new GameException(GAME_NOT_FOUND);

        List<GameStocksRedisDto> gameStocks = deserializeGameStocks(gameStocksJson);

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

    private List<GameStocksRedisDto> deserializeGameStocks(String gameStocksJson) {
        try {
            return objectMapper.readValue(gameStocksJson, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new RedisException(DESERIALIZE_FAILED);
        }
    }
}
