package com.org.mini_doodle.util;

import java.time.OffsetDateTime;

public class ValidationUtil {

    public ValidationUtil() {
    }

    public static void ensureStartBeforeEnd(OffsetDateTime start, OffsetDateTime end) {
        if (start == null || end == null) throw new IllegalArgumentException("start and end must be provided");
        if (!start.isBefore(end)) throw new IllegalArgumentException("startTime must be before endTime");
    }

    public static void ensureDurationWithin(long minutes, long min, long max) {
        if (minutes < min || minutes > max)
            throw new IllegalArgumentException("durationMinutes must be between " + min + " and " + max + " minutes");
    }

}
