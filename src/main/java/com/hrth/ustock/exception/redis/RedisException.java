package com.hrth.ustock.exception.redis;

import com.hrth.ustock.exception.common.CustomException;
import com.hrth.ustock.exception.common.CustomExceptionType;

public class RedisException extends CustomException {
    public RedisException(CustomExceptionType customExceptionType) {
        super(customExceptionType);
    }
}
