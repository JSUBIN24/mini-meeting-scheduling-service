package com.org.mini_doodle.repository;

import com.org.mini_doodle.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,Long> {
}
