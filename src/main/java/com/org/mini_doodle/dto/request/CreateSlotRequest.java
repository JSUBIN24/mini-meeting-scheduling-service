package com.org.mini_doodle.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.OffsetDateTime;

public record CreateSlotRequest(@NotNull(message = "Start time is required") OffsetDateTime startTime, @NotNull(message = "DurationMinutes is required" ) @Positive int durationMinutes) {
}
