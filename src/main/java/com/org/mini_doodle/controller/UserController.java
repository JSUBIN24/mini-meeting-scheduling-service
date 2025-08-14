package com.org.mini_doodle.controller;

import com.org.mini_doodle.domain.User;
import com.org.mini_doodle.repository.UserRepository;
import com.org.mini_doodle.service.UserService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

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
    public ResponseEntity<User>create(@RequestParam @Email String email, @RequestParam @NotBlank String name){
        User user = userService.createUser(email,name);
        return ResponseEntity.created(URI.create("User got created for the ID: " + user.getId())).body(user);
    }
}
