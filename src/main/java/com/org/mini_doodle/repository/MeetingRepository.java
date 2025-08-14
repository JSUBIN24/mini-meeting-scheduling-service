package com.org.mini_doodle.repository;

import com.org.mini_doodle.domain.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingRepository extends JpaRepository<Meeting,Long> {
}
