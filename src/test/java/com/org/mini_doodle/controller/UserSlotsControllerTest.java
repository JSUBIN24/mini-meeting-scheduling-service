package com.org.mini_doodle.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.org.mini_doodle.domain.Slot;
import com.org.mini_doodle.domain.SlotStatus;
import com.org.mini_doodle.dto.request.CreateSlotRequest;
import com.org.mini_doodle.dto.request.ModifySlotRequest;
import com.org.mini_doodle.dto.response.SlotResponse;
import com.org.mini_doodle.service.SlotService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserSlotsController.class)
@ExtendWith(MockitoExtension.class)
class UserSlotsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SlotService slotService;

    private ObjectMapper objectMapper;

    private Slot mockSlot;
    private final Long userId = 1L;
    private final Long slotId = 100L;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockSlot = new Slot();
        mockSlot.setId(slotId);
        mockSlot.setStartTime(OffsetDateTime.now());
        mockSlot.setEndTime(OffsetDateTime.now().plusHours(1));
        mockSlot.setStatus(SlotStatus.FREE);

        SlotResponse mockSlotResponse = SlotResponse.from(mockSlot);
    }

    @Test
    void create_ShouldReturnCreatedSlot_WhenValidRequest() throws Exception {
        // Arrange
        CreateSlotRequest request = new CreateSlotRequest(
                OffsetDateTime.now().plusDays(1),
                60
        );

        when(slotService.createSlotForUser(eq(userId), any(CreateSlotRequest.class)))
                .thenReturn(mockSlot);

        mockMvc.perform(post("/api/users/{userId}/slots", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(slotId));
        verify(slotService).createSlotForUser(eq(userId), any(CreateSlotRequest.class));
    }

    @Test
    void create_ShouldReturnBadRequest_WhenInvalidRequest() throws Exception {
        // Arrange
        CreateSlotRequest request = new CreateSlotRequest(null, 60);

        // Act & Assert
        mockMvc.perform(post("/api/users/{userId}/slots", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(slotService, never()).createSlotForUser(any(), any());
    }

    @Test
    void query_ShouldReturnPagedSlots_WhenValidRequest() throws Exception {
        // Arrange
        OffsetDateTime from = OffsetDateTime.now().minusDays(7);
        OffsetDateTime to = OffsetDateTime.now().plusDays(7);

        List<Slot> slots = List.of(mockSlot);
        Page<Slot> slotsPage = new PageImpl<>(slots, PageRequest.of(0, 20), 1);

        when(slotService.querySlotsForUser(eq(userId), eq(from), eq(to), eq(SlotStatus.FREE), any(Pageable.class)))
                .thenReturn(slotsPage);

        // Act & Assert
        mockMvc.perform(get("/api/users/{userId}/slots", userId)
                        .param("from", from.toString())
                        .param("to", to.toString())
                        .param("status", "FREE")
                        .param("page", "0")
                        .param("size", "20")
                        .param("sort", "startTime")
                        .param("dir", "ASC"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(slotId))
                .andExpect(jsonPath("$.totalElements").value(1));

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(slotService).querySlotsForUser(eq(userId), eq(from), eq(to), eq(SlotStatus.FREE), pageableCaptor.capture());

        Pageable capturedPageable = pageableCaptor.getValue();
        assertEquals(0, capturedPageable.getPageNumber());
        assertEquals(20, capturedPageable.getPageSize());
        assertEquals(Sort.Direction.ASC, capturedPageable.getSort().getOrderFor("startTime").getDirection());
    }

    @Test
    void query_ShouldUseDefaultParameters_WhenOptionalParamsNotProvided() throws Exception {
        // Arrange
        OffsetDateTime from = OffsetDateTime.now().minusDays(7);
        OffsetDateTime to = OffsetDateTime.now().plusDays(7);

        List<Slot> slots = List.of(mockSlot);
        Page<Slot> slotsPage = new PageImpl<>(slots, PageRequest.of(0, 20), 1);

        when(slotService.querySlotsForUser(eq(userId), eq(from), eq(to), eq(null), any(Pageable.class)))
                .thenReturn(slotsPage);

        // Act & Assert
        mockMvc.perform(get("/api/users/{userId}/slots", userId)
                        .param("from", from.toString())
                        .param("to", to.toString()))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(slotService).querySlotsForUser(eq(userId), eq(from), eq(to), eq(null), pageableCaptor.capture());

        Pageable capturedPageable = pageableCaptor.getValue();
        assertEquals(0, capturedPageable.getPageNumber());
        assertEquals(20, capturedPageable.getPageSize());
        assertEquals(Sort.Direction.ASC, capturedPageable.getSort().getOrderFor("startTime").getDirection());
    }

    @Test
    void query_ShouldHandleDescendingSort() throws Exception {
        // Arrange
        OffsetDateTime from = OffsetDateTime.now().minusDays(7);
        OffsetDateTime to = OffsetDateTime.now().plusDays(7);

        List<Slot> slots = List.of(mockSlot);
        Page<Slot> slotsPage = new PageImpl<>(slots, PageRequest.of(0, 10), 1);

        when(slotService.querySlotsForUser(any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(slotsPage);

        // Act & Assert
        mockMvc.perform(get("/api/users/{userId}/slots", userId)
                        .param("from", from.toString())
                        .param("to", to.toString())
                        .param("sort", "endTime")
                        .param("dir", "DESC"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(slotService).querySlotsForUser(any(), any(), any(), any(), pageableCaptor.capture());

        Pageable capturedPageable = pageableCaptor.getValue();
        assertEquals(Sort.Direction.DESC, capturedPageable.getSort().getOrderFor("endTime").getDirection());
    }


    @Test
    void modify_ShouldReturnBadRequest_WhenInvalidRequest() throws Exception {
        // Arrange
        ModifySlotRequest request = new ModifySlotRequest(null, null);

        // Act & Assert
        mockMvc.perform(patch("/api/users/{userId}/slots/{slotId}", userId, slotId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(slotService, never()).modifyTimes(any(), any(), any(), any());
    }

    @Test
    void mark_ShouldReturnUpdatedSlot_WhenValidStatus() throws Exception {
        // Arrange
        String status = "BUSY";

        when(slotService.markStatus(userId, slotId, SlotStatus.BUSY))
                .thenReturn(mockSlot);

        // Act & Assert
        mockMvc.perform(patch("/api/users/{userId}/slots/{slotId}/status", userId, slotId)
                        .param("status", status))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(slotId));

        verify(slotService).markStatus(userId, slotId, SlotStatus.BUSY);
    }

    @Test
    void mark_ShouldReturnBadRequest_WhenInvalidStatus() throws Exception {
        // Act & Assert
        mockMvc.perform(patch("/api/users/{userId}/slots/{slotId}/status", userId, slotId)
                        .param("status", "INVALID_STATUS"))
                .andExpect(status().isBadRequest());

        verify(slotService, never()).markStatus(any(), any(), any());
    }

    @Test
    void mark_ShouldReturnBadRequest_WhenStatusParameterMissing() throws Exception {
        // Act & Assert
        mockMvc.perform(patch("/api/users/{userId}/slots/{slotId}/status", userId, slotId))
                .andExpect(status().isBadRequest());

        verify(slotService, never()).markStatus(any(), any(), any());
    }

    @Test
    void delete_ShouldReturnNoContent_WhenValidRequest() throws Exception {
        // Arrange
        doNothing().when(slotService).deleteSlot(userId, slotId);

        // Act & Assert
        mockMvc.perform(delete("/api/users/{userId}/slots/{slotId}", userId, slotId))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(slotService).deleteSlot(userId, slotId);
    }

    // Additional edge case tests
    @Test
    void query_ShouldHandleCaseInsensitiveSortDirection() throws Exception {
        OffsetDateTime from = OffsetDateTime.now().minusDays(7);
        OffsetDateTime to = OffsetDateTime.now().plusDays(7);

        when(slotService.querySlotsForUser(any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/users/{userId}/slots", userId)
                        .param("from", from.toString())
                        .param("to", to.toString())
                        .param("dir", "desc"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(slotService).querySlotsForUser(any(), any(), any(), any(), pageableCaptor.capture());

        assertEquals(Sort.Direction.DESC, pageableCaptor.getValue().getSort().getOrderFor("startTime").getDirection());
    }
}
