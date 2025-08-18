package com.org.mini_doodle.service;

import com.org.mini_doodle.domain.*;
import com.org.mini_doodle.dto.request.ScheduleMeetingRequest;
import com.org.mini_doodle.exception.NotFoundException;
import com.org.mini_doodle.exception.OverlapConflictException;
import com.org.mini_doodle.repository.MeetingRepository;
import com.org.mini_doodle.repository.ParticipantRepository;
import com.org.mini_doodle.repository.SlotRepository;
import com.org.mini_doodle.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MeetingServiceTest {

    @Mock
    private SlotRepository slotRepository;
    @Mock
    private MeetingRepository meetingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ParticipantRepository participantRepository;

    @InjectMocks
    private MeetingService meetingService;

    private Slot slot;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        User owner = User.builder()
                .id(1L)
                .email("subin@example.com")
                .name("Subin Jerin")
                .build();

        Calendar calendar = Calendar.builder()
                .id(1L)
                .owner(owner)
                .name("Personal")
                .build();

        slot = Slot.builder()
                .id(10L)
                .status(SlotStatus.FREE)
                .startTime(OffsetDateTime.now())
                .endTime(OffsetDateTime.now().plusHours(1))
                .calendar(calendar)
                .build();
    }

    @Test
    void schedule_ShouldBookMeeting_WhenSlotIsFree() {
        // Arrange
        Long userId = 1L;
        ScheduleMeetingRequest req = new ScheduleMeetingRequest(
                slot.getId(),
                "Team Sync",
                "Discuss project updates",
                List.of(7L, 8L)
        );

        when(slotRepository.findById(slot.getId())).thenReturn(Optional.of(slot));
        when(meetingRepository.save(any(Meeting.class)))
                .thenAnswer(inv -> {
                    Meeting m = inv.getArgument(0);
                    m.setId(100L);
                    return m;
                });
        when(userRepository.findById(7L)).thenReturn(Optional.of(User.builder().id(7L).build()));
        when(userRepository.findById(8L)).thenReturn(Optional.of(User.builder().id(8L).build()));

        // Act
        Meeting result = meetingService.schedule(userId, req);

        // Assert
        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getTitle()).isEqualTo("Team Sync");
        assertThat(slot.getStatus()).isEqualTo(SlotStatus.BUSY);

        verify(meetingRepository, times(1)).save(any(Meeting.class));
        verify(participantRepository, times(1)).saveAll(anyList());
        verify(slotRepository, times(1)).save(slot);
    }

    @Test
    void schedule_ShouldThrow_WhenSlotNotFound() {
        when(slotRepository.findById(999L)).thenReturn(Optional.empty());

        ScheduleMeetingRequest req = new ScheduleMeetingRequest(
                999L, "Demo", "Test", List.of(1L)
        );

        assertThatThrownBy(() -> meetingService.schedule(1L, req))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Slot not found");
    }

    @Test
    void schedule_ShouldThrow_WhenSlotAlreadyBusy() {
        slot.setStatus(SlotStatus.BUSY);
        when(slotRepository.findById(slot.getId())).thenReturn(Optional.of(slot));

        ScheduleMeetingRequest req = new ScheduleMeetingRequest(
                slot.getId(), "Conflict", "Test", List.of(1L)
        );

        assertThatThrownBy(() -> meetingService.schedule(1L, req))
                .isInstanceOf(OverlapConflictException.class)
                .hasMessageContaining("Slot not available");
    }

    @Test
    void schedule_ShouldThrow_WhenParticipantNotFound() {
        when(slotRepository.findById(slot.getId())).thenReturn(Optional.of(slot));
        when(meetingRepository.save(any(Meeting.class)))
                .thenAnswer(inv -> {
                    Meeting m = inv.getArgument(0);
                    m.setId(200L);
                    return m;
                });
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        ScheduleMeetingRequest req = new ScheduleMeetingRequest(
                slot.getId(), "With ghost", "Unknown user", List.of(99L)
        );

        assertThatThrownBy(() -> meetingService.schedule(1L, req))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Participant user not found");
    }
}
