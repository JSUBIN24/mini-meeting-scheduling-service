package com.org.mini_doodle.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.OffsetDateTime;

public record CreateSlotRequest(@NotNull OffsetDateTime startTime, @Positive int durationMinutes) {
}
