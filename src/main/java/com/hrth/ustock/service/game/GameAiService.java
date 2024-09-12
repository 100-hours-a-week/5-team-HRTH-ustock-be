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
                너는 주식 모의투자 게임의 플레이어 AI 3개를 시뮬레이션 해야 하고, 힌트를 기반으로 종목을 구매할지 선택해.
                반드시 각 AI는 모두 다른 투자 성향을 가지고 있다고 가정하고, 선택의 결과에 차이가 있어야 해.
                즉, 반드시 각 AI가 독립된 상황이라고 가정하고 판단을 해야 해. 절대 다른 AI의 정보가 선택에 영향을 주어서는 안돼.
                종목은 하나만 사야 하는 것은 아니며, 힌트에 제공된 정보에 따라 모든 종목을 고려해서 여러 종목에 분산투자 하는 것도 고려해.
                단, 절대로 AI당 구매한 전체 종목의 가격(price)과 개수(quantity)의 곱이 AI가 보유 중인 예수금(budget)을 초과해서는 안돼.
                때문에 필요하다면 보유 종목을 판매해서 예수금을 충당해도 돼. 요청값에는 각 AI의 예수금, 보유 종목, 할당된 힌트 목록이 들어 있어.
                응답 타입은 반드시 JSON 형태의 문자열으로 반환해야 하고 JSON 값 이외의 다른 문자는 빼고 반환해. 그리고 닉네임을 키로 각 AI의 결과를 따로 반환해.
                ("COM1":{"sell":[{"id": 1, "name":"A 전자", "quantity": 3}],"buy":[{"id": 3, "name":"C 게임", "quantity": 5}],"reason":"선택한 이유"},
                "COM2":{...},"COM3":{...})
                여기서 id는 반드시 잘 요청 정보에 넣어서 보낸 종목 정보 안에 있는 id를 잘 기억해뒀다가 정확하게 매칭해서 반환해.
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
