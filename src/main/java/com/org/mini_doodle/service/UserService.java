package com.org.mini_doodle.service;

import com.org.mini_doodle.domain.Calendar;
import com.org.mini_doodle.domain.User;
import com.org.mini_doodle.repository.CalendarRepository;
import com.org.mini_doodle.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final CalendarRepository calendarRepository;

    private static final String DEFAULT_CALENDAR_NAME = "Personal";


    public UserService(UserRepository userRepository, CalendarRepository calendarRepository) {
        this.userRepository = userRepository;
        this.calendarRepository = calendarRepository;
    }


    @Transactional
    public User createUser(String email, String name) {

        User user = buildUser(email, name);
        user = userRepository.save(user);
        Calendar calendar = buildDefaultCalendar(user);
        calendarRepository.save(calendar);
        return user;
    }

    private static Calendar buildDefaultCalendar(User user) {
        return Calendar.builder()
                .owner(user)
                .name(DEFAULT_CALENDAR_NAME)
                .build();
    }

    private User buildUser(String email, String name) {
        return User.builder()
                .email(email)
                .name(name)
                .build();
    }
}
