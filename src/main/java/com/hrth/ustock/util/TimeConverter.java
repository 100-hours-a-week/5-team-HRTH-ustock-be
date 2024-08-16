package com.hrth.ustock.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class TimeConverter {
    private TimeConverter() {
    }

    public static String convertUnixTimeToKST(long unixTimestamp) {
        // Unix 타임을 Instant로 변환
        Instant instant = Instant.ofEpochSecond(unixTimestamp);

        // Instant를 ZonedDateTime으로 변환 (한국 표준시)
        ZonedDateTime kstDateTime = instant.atZone(ZoneId.of("Asia/Seoul"));

        // ZonedDateTime을 문자열로 포맷팅하여 반환
        return kstDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    public static long convertKSTToUnixTime(String zonedDateTimeString) {
        // 문자열을 ZonedDateTime으로 파싱 (한국 표준시 기준)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        ZonedDateTime kstDateTime = ZonedDateTime.parse(zonedDateTimeString, formatter.withZone(ZoneId.of("Asia/Seoul")));

        // ZonedDateTime을 Unix 타임스탬프로 변환
        return kstDateTime.toEpochSecond();
    }
}
