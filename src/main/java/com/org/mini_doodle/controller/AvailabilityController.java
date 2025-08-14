package com.org.mini_doodle.controller;

import com.org.mini_doodle.domain.TimeInterval;
import com.org.mini_doodle.dto.response.FreeBusyResponse;
import com.org.mini_doodle.service.AvailabilityService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/availability")
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    public AvailabilityController(AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @GetMapping("/free-busy")
    public ResponseEntity<Object> getAvailability(@PathVariable Long userId,
                                      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
                                      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
                                      @RequestParam(required = false) String granularity) {
        if (granularity == null) {
            FreeBusyResponse response = availabilityService.freeBusyForUser(userId, from, to);
            return ResponseEntity.ok(response);
        }
        Duration bucket = getParse(granularity);
        List<TimeInterval> intervals = availabilityService.bucketedForUser(userId, from, to, bucket);
        return ResponseEntity.ok(intervals);
    }

    private Duration getParse(String granularity) {
        try {
            return Duration.parse(granularity);
        }
        catch (DateTimeParseException ex){
            throw new IllegalArgumentException("Invalid granularity format. Expected ISO-8601 duration (e.g., PT30M, PT1H): " + granularity, ex);

        }
    }
}
