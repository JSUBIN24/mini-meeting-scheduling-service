package com.org.mini_doodle.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ScheduleMeetingRequest(@NotNull Long slotId, @NotBlank String title, String description,
                                     @NotEmpty List<Long> participantUserIds) {
}
