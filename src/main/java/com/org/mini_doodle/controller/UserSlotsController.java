package com.org.mini_doodle.controller;

import com.org.mini_doodle.domain.SlotStatus;
import com.org.mini_doodle.dto.request.CreateSlotRequest;
import com.org.mini_doodle.dto.request.ModifySlotRequest;
import com.org.mini_doodle.dto.response.SlotResponse;
import com.org.mini_doodle.service.SlotService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;

@Slf4j
@RestController
@RequestMapping("/api/users/{userId}/slots")
public class UserSlotsController {

    private final SlotService slotService;


    public UserSlotsController(SlotService slotService) {
        this.slotService = slotService;
    }

    @PostMapping
    public ResponseEntity<SlotResponse> create(@PathVariable Long userId, @Valid @RequestBody CreateSlotRequest req) {
        log.info("Creating slot for user={} from {} for {} minutes", userId, req.startTime(), req.durationMinutes());
        var slot = slotService.createSlotForUser(userId, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(SlotResponse.from(slot));
    }

    @GetMapping
    public ResponseEntity<Page<SlotResponse>> query(@PathVariable Long userId, @RequestParam OffsetDateTime from, @RequestParam OffsetDateTime to,
                                                    @RequestParam(required = false) String status, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size,
                                                    @RequestParam(defaultValue = "startTime") String sort, @RequestParam(defaultValue = "ASC") String dir) {
        log.info("Fetching slots for user={} from {} to {}, status={}", userId, from, to, status);
        SlotStatus slotStatus = status == null ? null : SlotStatus.valueOf(status);
        Pageable pageable = createPageable(page,size,sort,dir);

        var responsePages = slotService.querySlotsForUser(userId, from, to, slotStatus, pageable).map(SlotResponse::from);
        return ResponseEntity.ok(responsePages);
    }

    @PatchMapping("/{slotId}")
    public ResponseEntity<SlotResponse> modify(@PathVariable Long userId, @PathVariable Long slotId, @Valid @RequestBody ModifySlotRequest req) {
        log.info("Modifying slot id={} for user={}", slotId, userId);
        var slot = slotService.modifyTimes(userId, slotId, req.startTime(), req.endTime());
        return ResponseEntity.ok(SlotResponse.from(slot));
    }

    @PatchMapping("/{slotId}/status")
    public ResponseEntity<SlotResponse> mark(@PathVariable Long userId, @PathVariable Long slotId, @RequestParam String status) {
        log.info("Updating slot id={} for user={}", slotId, userId);
        var slot = slotService.markStatus(userId, slotId, SlotStatus.valueOf(status));
        return ResponseEntity.ok(SlotResponse.from(slot));
    }

    @DeleteMapping("/{slotId}")
    public ResponseEntity<Void> delete(@PathVariable Long userId, @PathVariable Long slotId) {
        log.info("Deleting slot id={} for user={}", slotId, userId);
        slotService.deleteSlot(userId, slotId);
        return ResponseEntity.noContent().build();
    }

    private Pageable createPageable(int page, int size, String sort, String dir) {
        Sort.Direction direction = "DESC".equalsIgnoreCase(dir) ?
                Sort.Direction.DESC : Sort.Direction.ASC;
        return PageRequest.of(page, size, Sort.by(direction, sort));
    }

}
