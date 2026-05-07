package com.raghu.queue_system.controller;

import com.raghu.queue_system.dto.UserRequestDTO;
import com.raghu.queue_system.dto.UserResponseDTO;
import com.raghu.queue_system.exception.UserNotFoundException;
import com.raghu.queue_system.model.User;
import com.raghu.queue_system.service.UserService;

import jakarta.validation.Valid;

import java.util.List;
import java.util.Optional;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public UserResponseDTO createUser(@Valid @RequestBody UserRequestDTO request) {
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());

        User savedUser = userService.saveUser(user);
        return userService.convertToDTO(savedUser);
    }

    @GetMapping
    public List<User> getUsers() {
        return userService.getUsers();
    }

    @GetMapping("/email")
    public UserResponseDTO getUserByEmail(@RequestParam String email) {
        User user = userService.getUser(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return userService.convertToDTO(user);
    }
}