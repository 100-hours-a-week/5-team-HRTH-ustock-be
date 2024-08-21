package com.hrth.ustock.util;

import org.springframework.data.util.Pair;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

public class DateConverter {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");

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
            LocalDate weekStart = getMonthStart(current);
            LocalDate weekEnd = getMonthEnd(current);
            ranges.add(Pair.of(weekStart.format(formatter), weekEnd.format(formatter)));
            current = weekEnd.plusDays(1);
        }
        return ranges;
    }

    public List<Pair<String, String>> getYearlyRanges(String start, String end) {
        List<Pair<String, String>> ranges = new ArrayList<>();
        LocalDate current = getLocalDate(start);
        LocalDate endDate = getLocalDate(end);
        while (!current.isAfter(endDate)) {
            LocalDate weekStart = getYearStart(current);
            LocalDate weekEnd = getYearEnd(current);
            ranges.add(Pair.of(weekStart.format(formatter), weekEnd.format(formatter)));
            current = weekEnd.plusDays(1);
        }
        return ranges;
    }

    private LocalDate getLocalDate(String date) {
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

    private LocalDate getYearStart(LocalDate date) {
        return date.with(TemporalAdjusters.firstDayOfYear());
    }

    private LocalDate getYearEnd(LocalDate date) {
        return date.with(TemporalAdjusters.lastDayOfYear());
    }
}
