package com.hrth.ustock.exception;

public class PortfolioNotFoundException extends RuntimeException {

    public PortfolioNotFoundException() {
        super();
    }

    public PortfolioNotFoundException(String message) {
        super(message);
    }
}
