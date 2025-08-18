package com.org.mini_doodle.controller;

import com.org.mini_doodle.domain.User;
import com.org.mini_doodle.repository.UserRepository;
import com.org.mini_doodle.service.UserService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@Validated
@Slf4j
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    public UserController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }


    @PostMapping
    public ResponseEntity<User>create(@RequestParam @Email(message = "Email must be in valid format") String email,
                                      @RequestParam @NotBlank(message = "Name is mandatory")
                                      @Pattern(regexp = "^[A-Za-z ]+$", message = "Name must contain only letters and spaces")
                                      String name){
        log.info("Creating new user with email={}", email);
        User user = userService.createUser(email,name);
        return ResponseEntity.created(URI.create("/api/users/" + user.getId())).body(user);
    }

    @GetMapping
    public List<User> getAllUser() {
        return userRepository.findAll();
    }
}
