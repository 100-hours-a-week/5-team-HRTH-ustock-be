package com.hrth.ustock.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrth.ustock.dto.stock.MarketResponseDto;
import com.hrth.ustock.dto.stock.StockResponseDto;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Slf4j
public class RedisJsonManager {
    public List<Map<String, String>> stringJsonConvert(String data) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(data, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            log.error("String -> JSON 변환을 실패했습니다.");
            throw new RuntimeException(e);
        }
    }

    public String jsonStringConvert(List<Map<String, String>> data) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            log.error("JSON -> String 변환을 실패했습니다.");
            throw new RuntimeException(e);
        }
    }

    public String dtoStringConvert(List<StockResponseDto> data) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            log.error("JSON -> String 변환을 실패했습니다.");
            throw new RuntimeException(e);
        }
    }

    public List<StockResponseDto> stringDtoConvert(String data) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(data, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            log.error("String -> JSON 변환을 실패했습니다.");
            throw new RuntimeException(e);
        }
    }

    public String mapStringConvert(Map<String, MarketResponseDto> data) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            log.error("JSON -> String 변환을 실패했습니다.");
            throw new RuntimeException(e);
        }
    }

    public Map<String, MarketResponseDto> stringMapConvert(String data) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(data, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            log.error("String -> JSON 변환을 실패했습니다.");
            throw new RuntimeException(e);
        }
    }
}
