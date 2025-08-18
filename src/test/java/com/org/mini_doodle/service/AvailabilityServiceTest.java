package com.org.mini_doodle.service;

import com.org.mini_doodle.domain.*;
import com.org.mini_doodle.dto.response.FreeBusyResponse;
import com.org.mini_doodle.exception.NotFoundException;
import com.org.mini_doodle.repository.CalendarRepository;
import com.org.mini_doodle.repository.SlotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@org.junit.jupiter.api.extension.ExtendWith(MockitoExtension.class)
class AvailabilityServiceTest {

    @Mock
    private SlotRepository slotRepository;

    @Mock
    private CalendarRepository calendarRepository;

    @InjectMocks
    private AvailabilityService availabilityService;

    private User subin;
    private Calendar calendar;
    private OffsetDateTime now;

    @BeforeEach
    void setup() {
        now = OffsetDateTime.now();
        subin = User.builder().id(1L).email("subin@example.com").name("Subin").build();
        calendar = Calendar.builder().id(10L).owner(subin).name("Personal").build();
    }

    @Test
    void freeBusyForUser_ShouldThrow_WhenCalendarNotFound() {
        when(calendarRepository.findByOwnerId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> availabilityService.freeBusyForUser(1L, now, now.plusHours(2)))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Calendar not found");
    }

    @Test
    void freeBusyForUser_ShouldReturnBusyAndFreeIntervals() {
        Slot busy1 = Slot.builder()
                .id(100L).calendar(calendar)
                .status(SlotStatus.BUSY)
                .startTime(now.plusMinutes(30))
                .endTime(now.plusMinutes(60))
                .build();

        Slot busy2 = Slot.builder()
                .id(101L).calendar(calendar)
                .status(SlotStatus.BUSY)
                .startTime(now.plusMinutes(50))
                .endTime(now.plusMinutes(90))
                .build();

        when(calendarRepository.findByOwnerId(1L)).thenReturn(Optional.of(calendar));
        when(slotRepository.findByCalendarAndStartTimeBetween(calendar, now, now.plusHours(2)))
                .thenReturn(List.of(busy1, busy2));

        FreeBusyResponse response = availabilityService.freeBusyForUser(1L, now, now.plusHours(2));

        assertThat(response.busy()).hasSize(1); // merged
        assertThat(response.busy().get(0).start()).isEqualTo(busy1.getStartTime());
        assertThat(response.busy().get(0).end()).isEqualTo(busy2.getEndTime());

        assertThat(response.free()).hasSize(2);
        assertThat(response.free().get(0).start()).isEqualTo(now);
        assertThat(response.free().get(0).end()).isEqualTo(busy1.getStartTime());
    }

    @Test
    void freeBusyForUser_ShouldReturnAllFree_WhenNoBusySlots() {
        when(calendarRepository.findByOwnerId(1L)).thenReturn(Optional.of(calendar));
        when(slotRepository.findByCalendarAndStartTimeBetween(calendar, now, now.plusHours(1)))
                .thenReturn(List.of());

        FreeBusyResponse response = availabilityService.freeBusyForUser(1L, now, now.plusHours(1));

        assertThat(response.busy()).isEmpty();
        assertThat(response.free()).containsExactly(new TimeInterval(now, now.plusHours(1)));
    }

    @Test
    void bucketedForUser_ShouldReturnBuckets() {
        List<TimeInterval> buckets = availabilityService.bucketedForUser(1L, now, now.plusHours(2), Duration.ofMinutes(30));

        assertThat(buckets).hasSize(4);
        assertThat(buckets.get(0).start()).isEqualTo(now);
        assertThat(buckets.get(0).end()).isEqualTo(now.plusMinutes(30));
        assertThat(buckets.getLast().end()).isEqualTo(now.plusHours(2));
    }
}
