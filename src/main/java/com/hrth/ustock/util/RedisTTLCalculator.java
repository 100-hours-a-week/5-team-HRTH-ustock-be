package com.hrth.ustock.util;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class RedisTTLCalculator {
    public long calculateTTLForMidnightKST() {
        // 한국 표준시 기준
        ZoneId zoneId = ZoneId.of("Asia/Seoul");
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        ZonedDateTime midnight = now.toLocalDate().plusDays(1).atStartOfDay(zoneId);
        Duration duration = Duration.between(now, midnight);
        return duration.getSeconds();
    }
}
