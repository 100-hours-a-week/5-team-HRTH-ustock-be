package com.hrth.ustock.service.game;

import com.hrth.ustock.dto.game.ai.GameAiSelectDto;
import com.hrth.ustock.dto.game.redis.GameHoldingsInfoDto;
import com.hrth.ustock.dto.game.redis.GameUserInfoDto;
import com.hrth.ustock.dto.game.stock.GameStockInfoResponseDto;
import com.hrth.ustock.entity.game.GameHint;
import com.hrth.ustock.repository.game.GameHintRepository;
import com.hrth.ustock.util.RedisJsonManager;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class GameAiService {
    private final OpenAiChatModel openAiChatModel;
    private final RedisJsonManager redisJsonManager;
    private final GameHintRepository gameHintRepository;

    public Map<String, GameAiSelectDto> getAiSelectResult(
            int year, List<GameUserInfoDto> playerList, List<GameStockInfoResponseDto> stockList) {

        Map<String, GameAiSelectDto> aiSelectResult = new HashMap<>();

        Map<String, Object> aiMap = new HashMap<>();

        int numbersOfAi = 3;
        for (int i = 1; i <= numbersOfAi; i++) {
            Map<String, Object> infoMap = new HashMap<>();

            GameUserInfoDto ai = playerList.get(i);

            AiInformation aiInformation = getAiInformation(ai);
            infoMap.put("account", aiInformation);

            List<Hint> selectedHints = getHints(year, stockList);
            infoMap.put("hint", selectedHints);

            aiMap.put(ai.getNickname(), infoMap);
        }

        String requestJson = redisJsonManager.mapStringConvert(aiMap);
        log.info(requestJson);

        String prompt = getPrompt();
        SystemMessage systemMessage = new SystemMessage(prompt);
        UserMessage userMessage = new UserMessage(requestJson);
        ChatResponse response = openAiChatModel.call(new Prompt(List.of(systemMessage, userMessage)));

        String responseJson = response.getResult().getOutput().getContent();
        log.info(responseJson);

        Map<String, Object> responseMap = redisJsonManager.stringMapConvert(responseJson);
        for (int i = 1; i <= numbersOfAi; i++) {
            String nickname = playerList.get(i).getNickname();
            aiSelectResult.put(nickname, convertResponseToDto(responseMap, nickname));
        }

        return aiSelectResult;
    }

    private static String getPrompt() {
        return """
               너는 주식 모의투자 게임의 플레이어 AI(각 AI는 투자 성향이 다르며, 독립적인 판단을 함) 3개를 시뮬레이션 해야해.
               반드시 하나의 종목만 구매해야하지만, 필요하다면 보유 종목을 판매해서 예수금을 충당해도 돼.
               응답 타입은 반드시 주어진 JSON 형태의 문자열으로만 반환(JSON 객체 외의 모든 문자는 전부 빼)해야 하고 닉네임을 키로 반환해.
               만약 구매 가격(price*quantity)이 budget보다 크다면 구매량을 줄여서 budget보다 작게 만들어.
               id는 요청 정보에 넣어서 보낸 종목 정보 안에 있는 id와 정확하게 매칭해서 반환해.
               ("COM1":{"sell":[{"id": 1, "name":"A 전자", "quantity": 3}],"buy":[{"id": 3, "name":"C 게임", "quantity": 5}], "reason":"선택한 이유"}, ...
               """;
    }

    private GameAiSelectDto convertResponseToDto(Map<String, Object> responseMap, String nickname) {
        String mapString = redisJsonManager.mapStringConvert((Map<String, Object>) responseMap.get(nickname));
        return redisJsonManager.deserializeObject(mapString, GameAiSelectDto.class);
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    private static class Hint {
        private long id;
        private int price;
        private String name;
        private String hint;
    }

    private List<Hint> getHints(int year, List<GameStockInfoResponseDto> stockList) {
        Collections.shuffle(stockList);

        int numbersOfHint = 3;
        List<GameStockInfoResponseDto> selectedStocks = new ArrayList<>();
        for (int j = 0; j < numbersOfHint; j++) {
            selectedStocks.add(stockList.get(j));
        }

        List<Long> stockIds = selectedStocks.stream()
                .map(GameStockInfoResponseDto::getStockId)
                .toList();
        List<GameHint> gameHints = gameHintRepository.findByStockIds(stockIds, year);
        List<Hint> selectedHints = new ArrayList<>();
        for (int j = 0; j < numbersOfHint; j++) {
            GameHint gameHint = gameHints.get(j * 3 + j);
            GameStockInfoResponseDto selectedStock = selectedStocks.get(j);

            Long id = selectedStock.getStockId();
            String name = selectedStock.getName();
            String hint = gameHint.getHint();
            selectedHints.add(new Hint(id, selectedStock.getCurrent(), name, hint));
        }

        return selectedHints;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    private static class AiInformation {
        private long budget;
        private List<GameHoldingsInfoDto> holdings;
    }

    private AiInformation getAiInformation(GameUserInfoDto player) {
        long budget = player.getBudget();
        List<GameHoldingsInfoDto> holdings = player.getHoldings();

        return new AiInformation(budget, holdings);
    }
}
