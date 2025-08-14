package com.org.mini_doodle.repository;

import com.org.mini_doodle.domain.Participant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {
}
