package com.org.mini_doodle.dto.response;


import com.org.mini_doodle.domain.Meeting;

import java.util.List;
import java.util.stream.Collectors;

public record MeetingResponse(Long id, Long slotId, String title, String description, List<Long> participantUserIds) {
    public static MeetingResponse from(Meeting m) {
        var ids = m.getParticipants() == null ? List.<Long>of() : m.getParticipants().stream().map(p -> p.getUser().getId()).collect(Collectors.toList());
        return new MeetingResponse(m.getId(), m.getSlot().getId(), m.getTitle(), m.getDescription(), ids);
    }
}
