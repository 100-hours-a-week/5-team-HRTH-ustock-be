package com.hrth.ustock.util;

import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@Component
public class DateConverter {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static final DateTimeFormatter redisFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd/HH/mm");

    public String minuteFormatter(int addMinute) {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));

        // 현재 분을 00분 또는 30분으로 맞춤
        int minute = now.getMinute() + addMinute;
        if (minute >= 30) {
            now = now.withMinute(30);
        } else {
            now = now.withMinute(0);
        }
        return now.format(redisFormatter);
    }

    public List<Pair<String, String>> getDailyRanges(String start, String end) {
        List<Pair<String, String>> ranges = new ArrayList<>();
        LocalDate current = getLocalDate(start);
        LocalDate endDate = getLocalDate(end);
        while (!current.isAfter(endDate)) {
            ranges.add(Pair.of(current.format(formatter), current.format(formatter)));
            current = current.plusDays(1);
        }
        return ranges;
    }

    public List<Pair<String, String>> getWeeklyRanges(String start, String end) {
        List<Pair<String, String>> ranges = new ArrayList<>();
        LocalDate current = getLocalDate(start);
        LocalDate endDate = getLocalDate(end);
        while (!current.isAfter(endDate)) {
            LocalDate weekStart = getWeekStart(current);
            LocalDate weekEnd = getWeekEnd(current);
            ranges.add(Pair.of(weekStart.format(formatter), weekEnd.format(formatter)));
            current = weekEnd.plusDays(1);
        }
        return ranges;
    }

    public List<Pair<String, String>> getMonthlyRanges(String start, String end) {
        List<Pair<String, String>> ranges = new ArrayList<>();
        LocalDate current = getLocalDate(start);
        LocalDate endDate = getLocalDate(end);
        while (!current.isAfter(endDate)) {
            LocalDate monthStart = getMonthStart(current);
            LocalDate monthEnd = getMonthEnd(current);
            ranges.add(Pair.of(monthStart.format(formatter), monthEnd.format(formatter)));
            current = monthEnd.plusDays(1);
        }
        return ranges;
    }

    public String getCurrentDate() {
        return LocalDate.now(ZoneId.of("Asia/Seoul")).format(formatter);
    }

    public String getStartDateOneYearAgo() {
        return LocalDate.now(ZoneId.of("Asia/Seoul")).minusYears(1).format(formatter);
    }

    public LocalDate getLocalDate(String date) {
        return LocalDate.parse(date, formatter);
    }

    private LocalDate getWeekStart(LocalDate date) {
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    private LocalDate getWeekEnd(LocalDate date) {
        return date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
    }

    private LocalDate getMonthStart(LocalDate date) {
        return date.with(TemporalAdjusters.firstDayOfMonth());
    }

    private LocalDate getMonthEnd(LocalDate date) {
        return date.with(TemporalAdjusters.lastDayOfMonth());
    }
}
