package com.hrth.ustock.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrth.ustock.dto.stock.StockResponseDto;
import com.hrth.ustock.exception.redis.RedisException;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

import static com.hrth.ustock.exception.redis.RedisExceptionType.DESERIALIZE_FAILED;
import static com.hrth.ustock.exception.redis.RedisExceptionType.SERIALIZE_FAILED;

@Slf4j
public class RedisJsonManager {
    public String dtoStringConvert(List<StockResponseDto> data) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new RedisException(SERIALIZE_FAILED);
        }
    }

    public List<StockResponseDto> stringDtoConvert(String data) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(data, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new RedisException(DESERIALIZE_FAILED);
        }
    }

    public String mapStringConvert(Map<String, Object> data) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new RedisException(SERIALIZE_FAILED);
        }
    }

    public Map<String, Object> stringMapConvert(String data) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(data, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new RedisException(DESERIALIZE_FAILED);
        }
    }
}
