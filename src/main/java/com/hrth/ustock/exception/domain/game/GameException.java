package com.hrth.ustock.exception.domain.game;

import com.hrth.ustock.exception.common.CustomException;
import com.hrth.ustock.exception.common.CustomExceptionType;

public class GameException extends CustomException {

    public GameException(CustomExceptionType customExceptionType) {
        super(customExceptionType);
    }
}
