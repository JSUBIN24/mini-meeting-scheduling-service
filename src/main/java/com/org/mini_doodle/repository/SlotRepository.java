package com.org.mini_doodle.repository;

import com.org.mini_doodle.domain.Calendar;
import com.org.mini_doodle.domain.Slot;
import com.org.mini_doodle.domain.SlotStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;
import java.util.List;

public interface SlotRepository extends JpaRepository<Slot,Long> {
    @Query("select s from Slot s where s.calendar = :calendar and s.endTime > :from and s.startTime < :to")
    List<Slot> findOverlapping(Calendar calendar, OffsetDateTime from, OffsetDateTime to);

    Page<Slot> findByCalendarAndStartTimeBetween(Calendar calendar, OffsetDateTime from, OffsetDateTime to, Pageable pageable);
    List<Slot> findByCalendarAndStartTimeBetween(Calendar calendar, OffsetDateTime from, OffsetDateTime to);

    Page<Slot> findByCalendarAndStartTimeBetweenAndStatus(Calendar calendar, OffsetDateTime from, OffsetDateTime to, SlotStatus status, Pageable pageable);

}
