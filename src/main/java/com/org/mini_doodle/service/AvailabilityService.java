package com.org.mini_doodle.service;

import com.org.mini_doodle.domain.Calendar;
import com.org.mini_doodle.domain.Slot;
import com.org.mini_doodle.domain.SlotStatus;
import com.org.mini_doodle.domain.TimeInterval;
import com.org.mini_doodle.dto.response.FreeBusyResponse;
import com.org.mini_doodle.exception.NotFoundException;
import com.org.mini_doodle.repository.CalendarRepository;
import com.org.mini_doodle.repository.SlotRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
public class AvailabilityService {

    private final SlotRepository slotRepository;
    private final CalendarRepository calendarRepository;

    public AvailabilityService(SlotRepository slotRepository, CalendarRepository calendarRepository) {
        this.slotRepository = slotRepository;
        this.calendarRepository = calendarRepository;
    }

    @Transactional(readOnly = true)
    public FreeBusyResponse freeBusyForUser(Long userId, OffsetDateTime from, OffsetDateTime to) {
        log.info("Calculating availability for user={} between {} and {}", userId, from, to);
        Calendar calendar = calendarRepository.findByOwnerId(userId)
                .orElseThrow(() -> new NotFoundException("Calendar not found for user: " + userId));

        List<Slot> slots = slotRepository.findByCalendarAndStartTimeBetween(calendar, from, to);

        List<TimeInterval> busyIntervals = extractBusyIntervals(slots);
        List<TimeInterval> mergedBusyIntervals = mergeOverlappingIntervals(busyIntervals);
        List<TimeInterval> freeIntervals = calculateFreeIntervals(from, to, mergedBusyIntervals);

        log.debug("Busy intervals={} Free intervals={}", busyIntervals, freeIntervals);
        return new FreeBusyResponse(mergedBusyIntervals, freeIntervals);
    }

    @Transactional(readOnly = true)
    public List<TimeInterval> bucketedForUser(Long userId, OffsetDateTime from, OffsetDateTime to, Duration bucket) {
        List<TimeInterval> result = new ArrayList<>();
        OffsetDateTime cursor = from;

        while (cursor.isBefore(to)) {
            OffsetDateTime end = cursor.plus(bucket);
            if (end.isAfter(to)) {
                end = to;
            }
            result.add(new TimeInterval(cursor, end));
            cursor = cursor.plus(bucket);
        }
        return result;
    }

    private List<TimeInterval> extractBusyIntervals(List<Slot> slots) {
        return slots.stream()
                .filter(slot -> slot.getStatus() == SlotStatus.BUSY)
                .map(slot -> new TimeInterval(slot.getStartTime(), slot.getEndTime()))
                .sorted(Comparator.comparing(TimeInterval::start))
                .toList();
    }

    private List<TimeInterval> mergeOverlappingIntervals(List<TimeInterval> intervals) {
        if (intervals.isEmpty()) {
            return new ArrayList<>();
        }

        List<TimeInterval> merged = new ArrayList<>();

        for (TimeInterval interval : intervals) {
            if (merged.isEmpty() || interval.start().isAfter(merged.getLast().end())) {
                merged.add(interval);
            } else {
                TimeInterval last = merged.removeLast();
                OffsetDateTime newEnd = last.end().isAfter(interval.end()) ? last.end() : interval.end();
                merged.add(new TimeInterval(last.start(), newEnd));
            }
        }

        return merged;
    }

    private List<TimeInterval> calculateFreeIntervals(OffsetDateTime from, OffsetDateTime to,
                                                      List<TimeInterval> busyIntervals) {
        List<TimeInterval> free = new ArrayList<>();
        OffsetDateTime cursor = from;

        for (TimeInterval busyInterval : busyIntervals) {
            if (busyInterval.start().isAfter(cursor)) {
                free.add(new TimeInterval(cursor, busyInterval.start()));
            }
            cursor = busyInterval.end().isAfter(cursor) ? busyInterval.end() : cursor;
        }

        if (cursor.isBefore(to)) {
            free.add(new TimeInterval(cursor, to));
        }

        return free;
    }
}
