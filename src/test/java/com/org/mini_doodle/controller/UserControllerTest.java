package com.org.mini_doodle.controller;

import com.org.mini_doodle.domain.User;
import com.org.mini_doodle.repository.UserRepository;
import com.org.mini_doodle.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void createUser_ShouldReturn201_WhenValidRequest() throws Exception {
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@example.com");
        mockUser.setName("Subin Jerin");

        when(userService.createUser(anyString(), anyString())).thenReturn(mockUser);

        mockMvc.perform(post("/api/users")
                        .param("email", "test@example.com")
                        .param("name", "Subin Jerin")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/users/1"))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.email", is("test@example.com")))
                .andExpect(jsonPath("$.name", is("Subin Jerin")));
    }

    @Test
    void createUser_ShouldReturn400_WhenInvalidEmail() throws Exception {
        mockMvc.perform(post("/api/users")
                        .param("email", "invalid-email")
                        .param("name", "Subin Jerin")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title", is("Validation Failed")))
                .andExpect(jsonPath("$.fieldErrors.['create.email']", containsString("Email must be in valid format")));
    }

    @Test
    void createUser_ShouldReturn400_WhenNameIsBlank() throws Exception {
        mockMvc.perform(post("/api/users")
                        .param("email", "test@example.com")
                        .param("name", "")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors['create.name']", anyOf(
                        containsString("Name is mandatory"),
                        containsString("Name must contain only letters and spaces")
                )));
    }

    @Test
    void createUser_ShouldReturn400_WhenNameHasNumbers() throws Exception {
        mockMvc.perform(post("/api/users")
                        .param("email", "test@example.com")
                        .param("name", "Subin123")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.['create.name']", containsString("Name must contain only letters and spaces")));
    }

    @Test
    void getAllUser_ShouldReturnListOfUsers() throws Exception {
        User user1 = new User();
        user1.setId(1L);
        user1.setEmail("s@example.com");
        user1.setName("Subin");

        User user2 = new User();
        user2.setId(2L);
        user2.setEmail("j@example.com");
        user2.setName("Jerin");

        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Subin")))
                .andExpect(jsonPath("$[1].name", is("Jerin")));
    }
}

