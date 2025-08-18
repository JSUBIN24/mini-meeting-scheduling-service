package com.org.mini_doodle.controller;

import com.org.mini_doodle.domain.Meeting;
import com.org.mini_doodle.domain.Participant;
import com.org.mini_doodle.domain.Slot;
import com.org.mini_doodle.domain.User;
import com.org.mini_doodle.dto.request.ScheduleMeetingRequest;
import com.org.mini_doodle.exception.GlobalExceptionHandler;
import com.org.mini_doodle.service.MeetingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserMeetingController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class UserMeetingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MeetingService meetingService;

    @Test
    void scheduleMeeting_ShouldReturn201_WhenValidRequest() throws Exception {


        User participant = User.builder().id(2L).email("francy@example.com").name("Francy").build();

        Meeting mockMeeting = Meeting.builder()
                .id(1L)
                .title("Project Sync")
                .description("Discuss timelines")
                .slot(Slot.builder().id(10L).build())
                .participants(List.of(
                        Participant.builder().id(100L).user(participant).build()
                ))
                .build();
        when(meetingService.schedule(any(Long.class), any(ScheduleMeetingRequest.class)))
                .thenReturn(mockMeeting);

        String requestJson = """
                {
                  "slotId": 10,
                  "title": "Project Sync",
                  "description": "Discuss timelines",
                  "participantUserIds": [2]
                }
                """;

        mockMvc.perform(post("/api/users/5/meetings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Project Sync"));
    }

    @Test
    void scheduleMeeting_ShouldReturn400_WhenTitleBlank() throws Exception {
        String requestJson = """
                {
                  "slotId": 10,
                  "title": ""
                }
                """;

        mockMvc.perform(post("/api/users/5/meetings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors['title']").exists());
    }
}