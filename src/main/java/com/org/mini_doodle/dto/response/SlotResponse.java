package com.org.mini_doodle.dto.response;



import com.org.mini_doodle.domain.Slot;
import com.org.mini_doodle.domain.SlotStatus;

import java.time.OffsetDateTime;

public record SlotResponse(Long id, SlotStatus status, OffsetDateTime startTime, OffsetDateTime endTime) {
    public static SlotResponse from(Slot s) {
        return new SlotResponse(s.getId(), s.getStatus(), s.getStartTime(), s.getEndTime());
    }
}
