package com.hrth.ustock.util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TimeDelay {

    private TimeDelay() {
    }

    public static void delay(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }
    }
}
