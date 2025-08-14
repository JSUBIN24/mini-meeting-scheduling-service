package com.org.mini_doodle.controller;

import com.org.mini_doodle.domain.SlotStatus;
import com.org.mini_doodle.dto.CreateSlotRequest;
import com.org.mini_doodle.dto.ModifySlotRequest;
import com.org.mini_doodle.dto.SlotResponse;
import com.org.mini_doodle.service.SlotService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/users/{userId}/slots")
public class UserSlotsController {

    private final SlotService slotService;


    public UserSlotsController(SlotService slotService) {
        this.slotService = slotService;
    }

    @PostMapping
    public ResponseEntity<SlotResponse> create(@PathVariable Long userId, @Valid @RequestBody CreateSlotRequest req) {
        var slotResponse = slotService.createSlotForUser(userId, req);
        return ResponseEntity.ok(SlotResponse.from(slotResponse));
    }

    @GetMapping
    public ResponseEntity<Page<SlotResponse>> query(@PathVariable Long userId, @RequestParam OffsetDateTime from, @RequestParam OffsetDateTime to,
                                                    @RequestParam(required = false) String status, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size,
                                                    @RequestParam(defaultValue = "startTime") String sort, @RequestParam(defaultValue = "ASC") String dir) {
        SlotStatus st = status == null ? null : SlotStatus.valueOf(status);
        Sort.Direction direction = "DESC".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort));
        var p = slotService.querySlotsForUser(userId, from, to, st, pageable).map(SlotResponse::from);
        return ResponseEntity.ok(p);
    }

    @PatchMapping("/{slotId}")
    public ResponseEntity<SlotResponse> modify(@PathVariable Long userId, @PathVariable Long slotId, @Valid @RequestBody ModifySlotRequest req) {
        var s = slotService.modifyTimes(userId, slotId, req.startTime(), req.endTime());
        return ResponseEntity.ok(SlotResponse.from(s));
    }

    @PatchMapping("/{slotId}/status")
    public ResponseEntity<SlotResponse> mark(@PathVariable Long userId, @PathVariable Long slotId, @RequestParam String status) {
        var s = slotService.markStatus(userId, slotId, SlotStatus.valueOf(status));
        return ResponseEntity.ok(SlotResponse.from(s));
    }

    @DeleteMapping("/{slotId}")
    public ResponseEntity<Void> delete(@PathVariable Long userId, @PathVariable Long slotId) {
        slotService.deleteSlot(userId, slotId);
        return ResponseEntity.noContent().build();
    }

}
