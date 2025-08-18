package com.org.mini_doodle.controller;

import com.org.mini_doodle.dto.request.ScheduleMeetingRequest;
import com.org.mini_doodle.dto.response.MeetingResponse;
import com.org.mini_doodle.service.MeetingService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/users/{userId}/meetings")
public class UserMeetingController {

    private final MeetingService meetingService;

    public UserMeetingController(MeetingService meetingService) {
        this.meetingService = meetingService;
    }

    @PostMapping
    public ResponseEntity<MeetingResponse> schedule(@PathVariable Long userId, @Valid @RequestBody ScheduleMeetingRequest req) {
        log.info("Booking meeting for slot id={} by user={} with title='{}'", req.slotId(), userId, req.title());
        var meeting = meetingService.schedule(userId, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(MeetingResponse.from(meeting));
    }
}
