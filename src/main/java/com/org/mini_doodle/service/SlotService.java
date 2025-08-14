package com.org.mini_doodle.service;

import com.org.mini_doodle.domain.Calendar;
import com.org.mini_doodle.domain.Slot;
import com.org.mini_doodle.domain.SlotStatus;
import com.org.mini_doodle.dto.request.CreateSlotRequest;
import com.org.mini_doodle.exception.NotFoundException;
import com.org.mini_doodle.exception.OverlapConflictException;
import com.org.mini_doodle.repository.SlotRepository;
import com.org.mini_doodle.util.Ownership;
import com.org.mini_doodle.util.ValidationUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class SlotService {

    private final SlotRepository slotRepository;
    private final UserService userService;
    private static final long MIN_DURATION_MIN = 5;
    private static final long MAX_DURATION_MIN = 24 * 60;


    public SlotService(SlotRepository slotRepository, UserService userService) {
        this.slotRepository = slotRepository;
        this.userService = userService;
    }


    @Transactional
    public Slot createSlotForUser(Long userId, CreateSlotRequest req) {
        Calendar calendar = userService.getPersonalCalendar(userId);
        OffsetDateTime endTime = req.startTime().plusMinutes(req.durationMinutes());

        validateSlotCreation(req.startTime(),endTime, req.durationMinutes());
        ensureNoOverlaps(calendar,req.startTime(),endTime,null);

        Slot slot = buildSlot(calendar,req.startTime(),endTime);
        return slotRepository.save(slot);
    }

    @Transactional(readOnly = true)
    public Page<Slot> querySlotsForUser(Long userId, OffsetDateTime from, OffsetDateTime to, SlotStatus status, Pageable pageable) {
        Calendar calendar = userService.getPersonalCalendar(userId);
        ValidationUtil.ensureStartBeforeEnd(from, to);

        if (status == null) return slotRepository.findByCalendarAndStartTimeBetween(calendar, from, to, pageable);
        return slotRepository.findByCalendarAndStartTimeBetweenAndStatus(calendar, from, to, status, pageable);
    }

    @Transactional
    public Slot modifyTimes(Long userId, Long slotId, OffsetDateTime newStart, OffsetDateTime newEnd) {
        Slot slot = findSlotAndEnsureOwnership(userId, slotId);

        ValidationUtil.ensureStartBeforeEnd(newStart, newEnd);
        ensureNoOverlaps(slot.getCalendar(), newStart, newEnd, slotId);

        slot.setStartTime(newStart);
        slot.setEndTime(newEnd);
        return slotRepository.save(slot);
    }

    @Transactional
    public Slot markStatus(Long userId, Long slotId, SlotStatus status) {
        Slot slot = findSlotAndEnsureOwnership(userId,slotId);

        if (status == SlotStatus.FREE && slot.getMeeting() != null){
            throw new OverlapConflictException("Cannot mark FREE: slot has a meeting");
        }

        slot.setStatus(status);
        return slotRepository.save(slot);
    }

    @Transactional
    public void deleteSlot(Long userId, Long slotId) {
        findSlotAndEnsureOwnership(userId,slotId);
        slotRepository.deleteById(slotId);
    }

    private void validateSlotCreation(OffsetDateTime startTime, OffsetDateTime endTime, long durationMinutes) {
        ValidationUtil.ensureDurationWithin(durationMinutes, MIN_DURATION_MIN, MAX_DURATION_MIN);
        ValidationUtil.ensureStartBeforeEnd(startTime, endTime);
    }

    private void ensureNoOverlaps(Calendar calendar, OffsetDateTime start, OffsetDateTime end, Long excludeSlotId) {
        List<Slot> overlaps = slotRepository.findOverlapping(calendar, start, end);

        if (excludeSlotId != null) {
            overlaps.removeIf(slot -> slot.getId().equals(excludeSlotId));
        }

        if (!overlaps.isEmpty()) {
            throw new OverlapConflictException("Overlapping slot exists");
        }
    }

    private Slot buildSlot(Calendar calendar, OffsetDateTime startTime, OffsetDateTime endTime) {
        return Slot.builder()
                .calendar(calendar)
                .startTime(startTime)
                .endTime(endTime)
                .status(SlotStatus.FREE)
                .build();
    }

    private Slot findSlotAndEnsureOwnership(Long userId, Long slotId) {
        Slot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new NotFoundException("Slot not found"));
        Ownership.ensureSlotBelongs(userId, slot);
        return slot;
    }

}
