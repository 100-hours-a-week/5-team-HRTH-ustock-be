package com.hrth.ustock.service.game;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrth.ustock.dto.game.GameStocksRedisDto;
import com.hrth.ustock.entity.game.GameInfo;
import com.hrth.ustock.entity.game.GameStockIndustry;
import com.hrth.ustock.entity.game.GameStockInfo;
import com.hrth.ustock.entity.game.GameStockYearly;
import com.hrth.ustock.entity.main.User;
import com.hrth.ustock.exception.domain.user.UserException;
import com.hrth.ustock.repository.UserRepository;
import com.hrth.ustock.repository.game.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.hrth.ustock.exception.domain.user.UserExceptionType.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class GamePlayService {
    private final int STOCK_COUNT = 8;

    private final UserRepository userRepository;
    private final GameHintRepository gameHintRepository;
    private final GameInfoRepository gameInfoRepository;
    private final GameNewsRepository gameNewsRepository;
    private final GameStockInfoRepository gameStockInfoRepository;
    private final GameStockYearlyRepository gameStockYearlyRepository;

    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, String> redisTemplate;

    public void startGame(Long userId, String nickname) throws JsonProcessingException {

        User user = userRepository.findById(userId).orElseThrow(() -> new UserException(USER_NOT_FOUND));

        GameInfo gameInfo = gameInfoRepository.save(GameInfo.builder()
                .user(user)
                .nickname(nickname)
                .build());

        List<GameStockInfo> stockInfoList = gameStockInfoRepository.findAll();
        Collections.shuffle(stockInfoList);

        List<GameStocksRedisDto> selectedList = new ArrayList<>();
        for (int i = 0; i < STOCK_COUNT; i++) {
            GameStockInfo gameStockInfo = stockInfoList.get(i);

            selectedList.add(GameStocksRedisDto.builder()
                    .id(gameStockInfo.getId())
                    .stockName(makeFakeStockName(i, gameStockInfo.getIndustry()))
                    .build()
            );
        }

        String stockIdList = objectMapper.writeValueAsString(selectedList);
        redisTemplate.opsForValue().set("game_stocks_" + gameInfo.getId(), stockIdList);
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

}
