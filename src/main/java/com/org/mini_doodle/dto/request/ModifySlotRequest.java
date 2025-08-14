package com.org.mini_doodle.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

public record ModifySlotRequest(@NotNull OffsetDateTime startTime, @NotNull OffsetDateTime endTime) {
}
