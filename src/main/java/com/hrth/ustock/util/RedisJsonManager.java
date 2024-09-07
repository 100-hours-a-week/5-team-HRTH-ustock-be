package com.hrth.ustock.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrth.ustock.exception.redis.RedisException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.hrth.ustock.exception.redis.RedisExceptionType.DESERIALIZE_FAILED;
import static com.hrth.ustock.exception.redis.RedisExceptionType.SERIALIZE_FAILED;

@Slf4j
@RequiredArgsConstructor
public class RedisJsonManager {
    private final ObjectMapper objectMapper;

    public <T> String serializeList(List<T> selectedList) {
        try {
            return objectMapper.writeValueAsString(selectedList);
        } catch (JsonProcessingException e) {
            throw new RedisException(SERIALIZE_FAILED);
        }
    }

    public <T> List<T> deserializeList(String jsonString, Class<T[]> clazz) {
        try {
            T[] array = objectMapper.readValue(jsonString, clazz);
            return Arrays.asList(array);
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
