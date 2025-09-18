package com.loopers.application.ranking;

public enum PeriodType {
    DAILY, WEEKLY, MONTHLY;

    public static PeriodType from(String s) {
        if (s == null) return DAILY;
        return switch (s.toLowerCase()) {
            case "weekly" -> WEEKLY;
            case "monthly" -> MONTHLY;
            default -> DAILY;
        };
    }
}
