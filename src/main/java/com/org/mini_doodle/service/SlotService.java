package com.org.mini_doodle.service;

import com.org.mini_doodle.domain.Calendar;
import com.org.mini_doodle.domain.Slot;
import com.org.mini_doodle.domain.SlotStatus;
import com.org.mini_doodle.dto.CreateSlotRequest;
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
        Calendar cal = userService.getPersonalCalendar(userId);
        var start = req.startTime();
        var end = start.plusMinutes(req.durationMinutes());
        ValidationUtil.ensureDurationWithin(req.durationMinutes(), MIN_DURATION_MIN, MAX_DURATION_MIN);
        ValidationUtil.ensureStartBeforeEnd(start, end);
        var overlaps = slotRepository.findOverlapping(cal, start, end);
        if (!overlaps.isEmpty()) throw new OverlapConflictException("Overlapping slot exists");
        var s = Slot.builder().calendar(cal).startTime(start).endTime(end).status(SlotStatus.FREE).build();
        return slotRepository.save(s);
    }

    @Transactional(readOnly = true)
    public Page<Slot> querySlotsForUser(Long userId, OffsetDateTime from, OffsetDateTime to, SlotStatus status, Pageable pageable) {
        Calendar cal = userService.getPersonalCalendar(userId);
        ValidationUtil.ensureStartBeforeEnd(from, to);
        if (status == null) return slotRepository.findByCalendarAndStartTimeBetween(cal, from, to, pageable);
        return slotRepository.findByCalendarAndStartTimeBetweenAndStatus(cal, from, to, status, pageable);
    }

    @Transactional
    public Slot modifyTimes(Long userId, Long slotId, OffsetDateTime newStart, OffsetDateTime newEnd) {
        var s = slotRepository.findById(slotId).orElseThrow(() -> new NotFoundException("Slot not found"));
        Ownership.ensureSlotBelongs(userId, s);
        ValidationUtil.ensureStartBeforeEnd(newStart, newEnd);
        var overlaps = slotRepository.findOverlapping(s.getCalendar(), newStart, newEnd);
        overlaps.removeIf(o -> o.getId().equals(slotId));
        if (!overlaps.isEmpty()) throw new OverlapConflictException("Overlapping slot exists");
        s.setStartTime(newStart);
        s.setEndTime(newEnd);
        return slotRepository.save(s);
    }

    @Transactional
    public Slot markStatus(Long userId, Long slotId, SlotStatus status) {
        var s = slotRepository.findById(slotId).orElseThrow(() -> new NotFoundException("Slot not found"));
        Ownership.ensureSlotBelongs(userId, s);
        if (status == SlotStatus.FREE && s.getMeeting() != null)
            throw new OverlapConflictException("Cannot mark FREE: slot has a meeting");
        s.setStatus(status);
        return slotRepository.save(s);
    }

    @Transactional
    public void deleteSlot(Long userId, Long slotId) {
        var s = slotRepository.findById(slotId).orElseThrow(() -> new NotFoundException("Slot not found"));
        Ownership.ensureSlotBelongs(userId, s);
        slotRepository.deleteById(slotId);
    }
}
