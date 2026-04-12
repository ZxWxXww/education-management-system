package com.edusmart.manager.common;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class StatusLabelMapper {
    private static final DateTimeFormatter SESSION_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private StatusLabelMapper() {
    }

    public static String courseStatusLabel(Integer status) {
        if (status == null) {
            return "";
        }
        return switch (status) {
            case 0 -> "\u505c\u7528";
            case 2 -> "\u6392\u8bfe\u4e2d";
            default -> "\u8fdb\u884c\u4e2d";
        };
    }

    public static String classStatusLabel(Integer status) {
        if (status == null) {
            return "";
        }
        return switch (status) {
            case 0 -> "\u505c\u7528";
            case 2 -> "\u62db\u751f\u4e2d";
            default -> "\u8fdb\u884c\u4e2d";
        };
    }

    public static String attendanceStatusLabel(String status) {
        if (status == null || status.isBlank()) {
            return "";
        }
        return switch (status.trim().toUpperCase()) {
            case "PRESENT" -> "\u6b63\u5e38";
            case "LATE" -> "\u8fdf\u5230";
            case "ABSENT" -> "\u7f3a\u52e4";
            case "LEAVE" -> "\u8bf7\u5047";
            case "EARLY_LEAVE" -> "\u65e9\u9000";
            default -> status;
        };
    }

    public static String attendanceExceptionTypeLabel(String exceptionType) {
        return attendanceStatusLabel(exceptionType);
    }

    public static String attendanceHandleStatusLabel(Integer isResolved) {
        return isResolved != null && isResolved == 1
                ? "\u5df2\u5904\u7406"
                : "\u5f85\u5904\u7406";
    }

    public static String studentAttendanceExceptionStatusLabel(Integer isResolved) {
        return isResolved != null && isResolved == 1
                ? "\u5df2\u786e\u8ba4"
                : "\u5f85\u8865\u5145\u8bf4\u660e";
    }

    public static String weekDayLabel(LocalDate date) {
        if (date == null) {
            return "";
        }
        return weekDayLabel(date.getDayOfWeek().getValue());
    }

    public static String weekDayLabel(int dayOfWeek) {
        return switch (dayOfWeek) {
            case 1 -> "\u5468\u4e00";
            case 2 -> "\u5468\u4e8c";
            case 3 -> "\u5468\u4e09";
            case 4 -> "\u5468\u56db";
            case 5 -> "\u5468\u4e94";
            case 6 -> "\u5468\u516d";
            case 7 -> "\u5468\u65e5";
            default -> "";
        };
    }

    public static String formatSessionTime(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            return "\u5f85\u6392\u8bfe";
        }
        return startTime.format(SESSION_TIME_FORMATTER) + "-" + endTime.format(SESSION_TIME_FORMATTER);
    }

    public static String clockTimeLabel(LocalDateTime time) {
        if (time == null) {
            return "";
        }
        return time.format(SESSION_TIME_FORMATTER);
    }
}
