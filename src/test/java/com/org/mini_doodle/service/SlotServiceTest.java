package com.org.mini_doodle.service;

import com.org.mini_doodle.domain.*;
import com.org.mini_doodle.dto.request.CreateSlotRequest;
import com.org.mini_doodle.exception.NotFoundException;
import com.org.mini_doodle.exception.OverlapConflictException;
import com.org.mini_doodle.repository.SlotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@org.junit.jupiter.api.extension.ExtendWith(MockitoExtension.class)
class SlotServiceTest {

    @Mock
    private SlotRepository slotRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private SlotService slotService;

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
    void createSlotForUser_ShouldCreate_WhenValid() {
        CreateSlotRequest req = new CreateSlotRequest(now, 30);

        when(userService.getPersonalCalendar(1L)).thenReturn(calendar);
        when(slotRepository.findOverlapping(calendar, req.startTime(), req.startTime().plusMinutes(req.durationMinutes())))
                .thenReturn(List.of());
        when(slotRepository.save(any(Slot.class))).thenAnswer(inv -> {
            Slot s = inv.getArgument(0);
            s.setId(100L);
            return s;
        });

        Slot slot = slotService.createSlotForUser(1L, req);

        assertThat(slot.getId()).isEqualTo(100L);
        assertThat(slot.getStatus()).isEqualTo(SlotStatus.FREE);
        assertThat(slot.getStartTime()).isEqualTo(now);
        verify(slotRepository).save(any(Slot.class));
    }

    @Test
    void createSlotForUser_ShouldThrow_WhenOverlappingExists() {
        CreateSlotRequest req = new CreateSlotRequest(now, 30);

        when(userService.getPersonalCalendar(1L)).thenReturn(calendar);
        when(slotRepository.findOverlapping(any(), any(), any())).thenReturn(List.of(new Slot()));

        assertThatThrownBy(() -> slotService.createSlotForUser(1L, req))
                .isInstanceOf(OverlapConflictException.class)
                .hasMessageContaining("Overlapping slot exists");
    }


    @Test
    void querySlotsForUser_ShouldReturnPage() {
        PageRequest pageable = PageRequest.of(0, 10, Sort.by("startTime"));
        Slot slot = Slot.builder().id(100L).calendar(calendar).startTime(now).endTime(now.plusMinutes(30)).status(SlotStatus.FREE).build();

        when(userService.getPersonalCalendar(1L)).thenReturn(calendar);
        when(slotRepository.findByCalendarAndStartTimeBetween(calendar, now, now.plusHours(1), pageable))
                .thenReturn(new PageImpl<>(List.of(slot)));

        Page<Slot> result = slotService.querySlotsForUser(1L, now, now.plusHours(1), null, pageable);

        assertThat(result).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(100L);
    }


    @Test
    void modifyTimes_ShouldUpdate_WhenValid() {
        Slot slot = Slot.builder()
                .id(100L).calendar(calendar)
                .startTime(now).endTime(now.plusMinutes(30)).status(SlotStatus.FREE).build();

        when(slotRepository.findById(100L)).thenReturn(Optional.of(slot));
        when(slotRepository.findOverlapping(any(), any(), any())).thenReturn(new ArrayList<>());
        when(slotRepository.save(slot)).thenReturn(slot);

        Slot updated = slotService.modifyTimes(1L, 100L, now.plusMinutes(10), now.plusMinutes(40));

        assertThat(updated.getStartTime()).isEqualTo(now.plusMinutes(10));
        assertThat(updated.getEndTime()).isEqualTo(now.plusMinutes(40));
    }


    @Test
    void markStatus_ShouldUpdateStatus_WhenFree() {
        Slot slot = Slot.builder()
                .id(100L).calendar(calendar)
                .startTime(now).endTime(now.plusMinutes(30))
                .status(SlotStatus.FREE)
                .build();

        when(slotRepository.findById(100L)).thenReturn(Optional.of(slot));
        when(slotRepository.save(slot)).thenReturn(slot);

        Slot updated = slotService.markStatus(1L, 100L, SlotStatus.BUSY);

        assertThat(updated.getStatus()).isEqualTo(SlotStatus.BUSY);
    }

    @Test
    void markStatus_ShouldThrow_WhenMarkFreeButMeetingAttached() {
        Meeting meeting = Meeting.builder().id(200L).title("Sync").build();
        Slot slot = Slot.builder()
                .id(100L).calendar(calendar)
                .meeting(meeting)
                .status(SlotStatus.BUSY)
                .startTime(now).endTime(now.plusMinutes(30)).build();

        when(slotRepository.findById(100L)).thenReturn(Optional.of(slot));

        assertThatThrownBy(() -> slotService.markStatus(1L, 100L, SlotStatus.FREE))
                .isInstanceOf(OverlapConflictException.class)
                .hasMessageContaining("slot has a meeting");
    }


    @Test
    void deleteSlot_ShouldDelete_WhenValid() {
        Slot slot = Slot.builder().id(100L).calendar(calendar).build();

        when(slotRepository.findById(100L)).thenReturn(Optional.of(slot));

        slotService.deleteSlot(1L, 100L);

        verify(slotRepository).deleteById(100L);
    }

    @Test
    void deleteSlot_ShouldThrow_WhenNotFound() {
        when(slotRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> slotService.deleteSlot(1L, 999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Slot not found");
    }
}
