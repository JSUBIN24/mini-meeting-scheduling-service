package com.org.mini_doodle.controller;

import com.org.mini_doodle.domain.TimeInterval;
import com.org.mini_doodle.dto.response.FreeBusyResponse;
import com.org.mini_doodle.service.AvailabilityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AvailabilityController.class)
class AvailabilityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AvailabilityService availabilityService;

    private OffsetDateTime from;
    private OffsetDateTime to;

    @BeforeEach
    void setUp() {
        from = OffsetDateTime.parse("2025-08-15T10:00:00Z");
        to = OffsetDateTime.parse("2025-08-15T12:00:00Z");
    }

    @Test
    void shouldReturnFreeBusyWhenGranularityIsNull() throws Exception {
        FreeBusyResponse mockResponse = new FreeBusyResponse(
                List.of(new TimeInterval(from, from.plusHours(1))),
                List.of(new TimeInterval(from.plusHours(1), to))
        );

        Mockito.when(availabilityService.freeBusyForUser(eq(1L), any(), any()))
                .thenReturn(mockResponse);

        mockMvc.perform(get("/api/users/1/availability/free-busy")
                        .param("from", from.toString())
                        .param("to", to.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.busy[0].start").value("2025-08-15T10:00:00Z"))
                .andExpect(jsonPath("$.free[0].end").value("2025-08-15T12:00:00Z"));
    }

    @Test
    void shouldReturnBucketedAvailabilityWhenGranularityIsProvided() throws Exception {
        List<TimeInterval> mockIntervals = List.of(
                new TimeInterval(from, from.plusMinutes(30)),
                new TimeInterval(from.plusMinutes(30), from.plusMinutes(60))
        );

        Mockito.when(availabilityService.bucketedForUser(eq(1L), any(), any(), eq(Duration.ofMinutes(30))))
                .thenReturn(mockIntervals);

        mockMvc.perform(get("/api/users/1/availability/free-busy")
                        .param("from", from.toString())
                        .param("to", to.toString())
                        .param("granularity", "PT30M")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].start").value("2025-08-15T10:00:00Z"))
                .andExpect(jsonPath("$[1].end").value("2025-08-15T11:00:00Z"));
    }

    @Test
    void shouldReturnBadRequestWhenGranularityIsInvalid() throws Exception {
        mockMvc.perform(get("/api/users/1/availability/free-busy")
                        .param("from", from.toString())
                        .param("to", to.toString())
                        .param("granularity", "30") // invalid format
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.detail").value(
                        "Invalid granularity format. Expected ISO-8601 duration (e.g., PT30M, PT1H): 30"
                ));
    }
}
