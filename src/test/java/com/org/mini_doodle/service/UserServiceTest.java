package com.org.mini_doodle.service;

import com.org.mini_doodle.domain.Calendar;
import com.org.mini_doodle.domain.User;
import com.org.mini_doodle.repository.CalendarRepository;
import com.org.mini_doodle.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CalendarRepository calendarRepository;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createUser_ShouldSaveUserAndDefaultCalendar() {
        // Arrange
        User mockUser = User.builder().id(1L).email("francy@example.com").name("Francy").build();
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(calendarRepository.save(any(Calendar.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User result = userService.createUser("francy@example.com", "Francy");

        // Assert
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("francy@example.com");
        verify(userRepository, times(1)).save(any(User.class));
        verify(calendarRepository, times(1)).save(any(Calendar.class));
    }

    @Test
    void getPersonalCalendar_ShouldReturnCalendar_WhenExists() {
        // Arrange
        User mockUser = User.builder().id(1L).email("subin@example.com").name("Subin").build();
        Calendar mockCalendar = Calendar.builder().id(100L).owner(mockUser).name("Personal").build();
        when(calendarRepository.findByOwnerId(1L)).thenReturn(Optional.of(mockCalendar));

        // Act
        Calendar result = userService.getPersonalCalendar(1L);

        // Assert
        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getName()).isEqualTo("Personal");
    }

    @Test
    void getPersonalCalendar_ShouldThrow_WhenNotExists() {
        // Arrange
        when(calendarRepository.findByOwnerId(99L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> userService.getPersonalCalendar(99L))
                .isInstanceOf(java.util.NoSuchElementException.class);
    }
}
