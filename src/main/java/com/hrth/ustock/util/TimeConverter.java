package com.hrth.ustock.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class TimeConverter {

    public static String convertUnixTimeToKST(long unixTimestamp) {

        Instant instant = Instant.ofEpochSecond(unixTimestamp);
        ZonedDateTime kstDateTime = instant.atZone(ZoneId.of("Asia/Seoul"));
        return kstDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    public static long convertKSTToUnixTime(String zonedDateTimeString) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        ZonedDateTime kstDateTime = ZonedDateTime.parse(zonedDateTimeString, formatter.withZone(ZoneId.of("Asia/Seoul")));
        return kstDateTime.toEpochSecond();
    }
}
