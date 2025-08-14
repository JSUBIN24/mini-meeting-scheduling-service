package com.org.mini_doodle.service;

import com.org.mini_doodle.domain.*;
import com.org.mini_doodle.dto.request.ScheduleMeetingRequest;
import com.org.mini_doodle.exception.NotFoundException;
import com.org.mini_doodle.exception.OverlapConflictException;
import com.org.mini_doodle.repository.MeetingRepository;
import com.org.mini_doodle.repository.ParticipantRepository;
import com.org.mini_doodle.repository.SlotRepository;
import com.org.mini_doodle.repository.UserRepository;
import com.org.mini_doodle.util.Ownership;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class MeetingService {

    private final SlotRepository slotRepository;
    private final MeetingRepository meetingRepository;
    private final UserRepository userRepository;
    private final ParticipantRepository participantRepository;

    public MeetingService(SlotRepository slotRepository, MeetingRepository meetingRepository, UserRepository userRepository, ParticipantRepository participantRepository) {
        this.slotRepository = slotRepository;
        this.meetingRepository = meetingRepository;
        this.userRepository = userRepository;
        this.participantRepository = participantRepository;
    }

    @Transactional
    public Meeting schedule(Long userId, ScheduleMeetingRequest req) {
        Slot slot = findAndValidateSlot(userId,req.slotId());
        Meeting meeting = createMeeting(req, slot);
        addParticipants(meeting, req.participantUserIds());
        markSlotAsBusy(slot, meeting);
        return meeting;
    }

    private Meeting createMeeting(ScheduleMeetingRequest req, Slot slot) {
        Meeting meeting = Meeting.builder()
                .slot(slot)
                .title(req.title())
                .description(req.description())
                .build();
        return meetingRepository.save(meeting);
    }


    private Slot findAndValidateSlot(Long userId, Long slotId) {
        Slot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new NotFoundException("Slot not found"));

        Ownership.ensureSlotBelongs(userId, slot);

        if (slot.getStatus() == SlotStatus.BUSY || slot.getMeeting() != null) {
            throw new OverlapConflictException("Slot not available");
        }

        return slot;
    }

    private void addParticipants(Meeting meeting, List<Long> participantUserIds) {
        Set<Long> uniqueUserIds = new LinkedHashSet<>(participantUserIds);
        List<Participant> participants = createParticipantList(meeting, uniqueUserIds);
        participantRepository.saveAll(participants);
    }

    private List<Participant> createParticipantList(Meeting meeting, Set<Long> userIds) {
        return userIds.stream()
                .map(userId -> createParticipant(meeting, userId))
                .toList();
    }

    private Participant createParticipant(Meeting meeting, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Participant user not found: " + userId));

        return Participant.builder()
                .meeting(meeting)
                .user(user)
                .build();
    }

    private void markSlotAsBusy(Slot slot, Meeting meeting) {
        slot.setStatus(SlotStatus.BUSY);
        slot.setMeeting(meeting);
        slotRepository.save(slot);
    }
}
