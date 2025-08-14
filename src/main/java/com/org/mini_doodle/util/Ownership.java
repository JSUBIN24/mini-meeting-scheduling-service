package com.org.mini_doodle.util;

import com.org.mini_doodle.domain.Slot;
import com.org.mini_doodle.exception.OwnershipViolationException;

public class Ownership {

    public Ownership() {
    }

    public static void ensureSlotBelongs(Long userId, Slot s) {
        if (!s.getCalendar().getOwner().getId().equals(userId))
            throw new OwnershipViolationException("Slot does not belong to the user's calendar");
    }
}
