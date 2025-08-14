package com.org.mini_doodle.repository;

import com.org.mini_doodle.domain.Calendar;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CalendarRepository extends JpaRepository<Calendar,Long> {
    Optional<Calendar> findByOwnerId(Long ownerId);
}
