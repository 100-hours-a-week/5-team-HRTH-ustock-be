package com.hrth.ustock.service.game;

import com.hrth.ustock.dto.game.ai.GameAiSelectDto;
import com.hrth.ustock.dto.game.ai.GameAiStockDto;
import com.hrth.ustock.dto.game.redis.GameHoldingsInfoDto;
import com.hrth.ustock.dto.game.redis.GameUserInfoDto;
import com.hrth.ustock.dto.game.stock.GameStockInfoResponseDto;
import com.hrth.ustock.entity.game.GameActing;
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
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.hrth.ustock.entity.game.GameActing.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class GameAiService {
    public static final int NUMBERS_OF_AI = 3;

    private final OpenAiChatModel openAiChatModel;
    private final RedisJsonManager redisJsonManager;
    private final GameHintRepository gameHintRepository;

    private final OpenAiChatOptions chatModel = OpenAiChatOptions.builder()
            .withModel("gpt-4o-mini")
            .withTemperature(0.7f)
            .build();

    public Map<String, GameAiSelectDto> getAiSelectResult(
            int year, List<GameUserInfoDto> playerList, List<GameStockInfoResponseDto> stockList) {

        Map<String, GameAiSelectDto> aiSelectResult = new HashMap<>();

        Map<String, Object> sellResponse = requestToAiTradeStock(year, setSellInfoMap(playerList), SELL);
        log.info("sellResponse: {}", sellResponse);

        for (int i = 1; i <= NUMBERS_OF_AI; i++) {
            String aiNickname = playerList.get(i).getNickname();
            if (sellResponse.get(aiNickname).toString().equals("{}")) {
                continue;
            }

            List<GameAiStockDto> stocks = ((List<Map<String, Object>>) sellResponse.get(aiNickname)).stream()
                    .map(this::convertMapToDto)
                    .toList();
            GameAiSelectDto gameAiSelectDto = new GameAiSelectDto();
            gameAiSelectDto.setSell(stocks);

            aiSelectResult.put(aiNickname, gameAiSelectDto);
        }

        Map<String, Object> buyResponse = requestToAiTradeStock(year, setBuyInfoMap(year, playerList, stockList), BUY);
        log.info("buyResponse: {}", buyResponse);

        for (int i = 1; i <= NUMBERS_OF_AI; i++) {
            String aiNickname = playerList.get(i).getNickname();
            if (buyResponse.get(aiNickname).toString().equals("{}")) {
                continue;
            }

            Map<String, Object> buyMap = (Map<String, Object>) buyResponse.get(aiNickname);

            List<GameAiStockDto> stocks = ((List<Map<String, Object>>) buyMap.get("list")).stream()
                    .map(this::convertMapToDto)
                    .toList();
            String reason = (String) buyMap.get("reason");

            GameAiSelectDto gameAiSelectDto = aiSelectResult.get(aiNickname);
            gameAiSelectDto.setBuy(stocks);
            gameAiSelectDto.setReason(reason);

            aiSelectResult.put(aiNickname, gameAiSelectDto);
        }

        return aiSelectResult;
    }

    private Map<String, Object> setBuyInfoMap(
            int year, List<GameUserInfoDto> playerList, List<GameStockInfoResponseDto> stockList) {

        Map<String, Object> buyInfoMap = new HashMap<>();

        for (int i = 1; i <= NUMBERS_OF_AI; i++) {
            GameUserInfoDto ai = playerList.get(i);

            Map<String, Object> requestMap = new HashMap<>();
            List<Hint> selectedHints = getHints(year, stockList);
            requestMap.put("budget", ai.getBudget());
            requestMap.put("hints", selectedHints);

            buyInfoMap.put(ai.getNickname(), requestMap);
        }

        log.info("buyInfoMap: {}", buyInfoMap);

        return buyInfoMap;
    }

    private Map<String, Object> setSellInfoMap(List<GameUserInfoDto> playerList) {
        Map<String, Object> sellInfoMap = new HashMap<>();

        for (int i = 1; i <= NUMBERS_OF_AI; i++) {
            GameUserInfoDto ai = playerList.get(i);

            AiInformation aiInformation = getAiInformation(ai);
            sellInfoMap.put(ai.getNickname(), aiInformation);
        }

        log.info("sellInfoMap: {}", sellInfoMap);

        return sellInfoMap;
    }

    private String makeBuyPrompt() {
        return """
                너는 주식 모의투자 게임의 플레이어 AI(각 AI는 투자 성향이 다르며, 독립적인 판단을 함) 3개를 시뮬레이션 해야해.
                나는 각 AI가 보유 중인 예수금과 할당된 힌트에 대한 정보를 줄거고, 너는 이걸 바탕으로 구매 여부를 결정하면 돼.
                응답 타입은 반드시 주어진 JSON 형태의 문자열으로만 반환(JSON 객체 외의 모든 문자는 전부 빼)해야 하고 닉네임을 키로 반환해.
                id는 요청 정보에 넣어서 보낸 종목 정보 안에 있는 id와 정확하게 매칭해서 반환해.
                판매할 종목의 아이디, 이름, 수량을 객체로 묶은 후, 리스트로 반환해. 그리고 왜 그런 선택을 했는지 이유도 같이 반환해.
                아래의 예시를 참고해.
                {"COM1":{"list":[{"id": 1, "name":"A 전자", "quantity": 3}, {"id":2, "name":"C 게임", "quantity": 5}],
                "reason":"종목을 선택한 이유"}, "COM2":{...},...}
                """;
    }

    private String makeSellPrompt() {
        return """
                너는 주식 모의투자 게임의 플레이어 AI(각 AI는 투자 성향이 다르며, 독립적인 판단을 함) 3개를 시뮬레이션 해야해.
                나는 각 AI가 보유 중인 종목에 대한 정보와 현재 종목 가격에 대한 정보를 줄거고, 너는 이걸로 종목 판매 여부를 선택해야 해.
                반드시 보유 중인 종목을 판매할 필요는 없고, 최대한 수익이 발생했을 때만 판매해.
                응답 타입은 반드시 주어진 JSON 형태의 문자열으로만 반환(JSON 객체 외의 모든 문자는 전부 빼)해야 하고 닉네임을 키로 반환해.
                id는 요청 정보에 넣어서 보낸 종목 정보 안에 있는 id와 정확하게 매칭해서 반환해.
                판매할 종목의 아이디, 이름, 수량을 객체로 묶은 후, 리스트로 반환해. 아래의 예시를 참고해.
                {"COM1":[{"id": 1, "name":"A 전자", "quantity": 3}, {"id":2, "name":"C 게임", "quantity": 5}], "COM2":{...},...}
                """;
    }

    private Map<String, Object> requestToAiTradeStock(int year, Map<String, Object> infoMap, GameActing acting) {

        if (acting == SELL && year == 2014) {
            Map<String, Object> emptyMap = new HashMap<>();

            for (int i = 1; i <= NUMBERS_OF_AI; i++) {
                emptyMap.put("COM" + i, new ArrayList<>());
            }

            return emptyMap;
        }

        String infoJson = redisJsonManager.mapStringConvert(infoMap);

        String requestPrompt = acting == BUY ? makeBuyPrompt() : makeSellPrompt();
        SystemMessage systemMessage = new SystemMessage(requestPrompt);
        UserMessage userMessage = new UserMessage(infoJson);

        ChatResponse response = openAiChatModel.call(new Prompt(List.of(systemMessage, userMessage), chatModel));
        String responseJson = response.getResult().getOutput().getContent();
        log.info("responseJson: {}", responseJson);

        if (responseJson.startsWith("```"))
            responseJson = responseJson.replace("```", "").replace("json", "");

        return redisJsonManager.stringMapConvert(responseJson);
    }

    private GameAiStockDto convertMapToDto(Map<String, Object> map) {
        String mapString = redisJsonManager.mapStringConvert(map);
        return redisJsonManager.deserializeObject(mapString, GameAiStockDto.class);
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
        for (int j = 0; j < 3; j++) {
            GameStockInfoResponseDto stock = stockList.get(j);

            if (stock.getCurrent() == 0)
                numbersOfHint--;

            selectedStocks.add(stock);
        }

        stockList.sort((a, b) -> (int) (a.getStockId() - b.getStockId()));

        List<Long> stockIds = selectedStocks.stream()
                .map(GameStockInfoResponseDto::getStockId)
                .toList();
        List<GameHint> gameHints = gameHintRepository.findByStockIds(stockIds, year + 1);
        List<Hint> selectedHints = new ArrayList<>();
        for (int j = 0; j < numbersOfHint; j++) {
            GameHint gameHint = gameHints.get(j * 3 + j);
            GameStockInfoResponseDto selectedStock = selectedStocks.get(j);

            long id = selectedStock.getStockId();
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
