package com.raghu.queue_system.controller;

import com.raghu.queue_system.dto.ApiResponse;
import com.raghu.queue_system.dto.UserRequestDTO;
import com.raghu.queue_system.dto.UserResponseDTO;
import com.raghu.queue_system.exception.UserNotFoundException;
import com.raghu.queue_system.model.User;
import com.raghu.queue_system.model.UserRole;
import com.raghu.queue_system.service.UserService;

import jakarta.validation.Valid;

import java.util.List;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ApiResponse<UserResponseDTO> createUser(@Valid @RequestBody UserRequestDTO request) {
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setRole(request.getRole() != null ? request.getRole() : UserRole.PATIENT);

        User savedUser = userService.saveUser(user);
        UserResponseDTO response = userService.convertToDTO(savedUser);
        return ApiResponse.success("User created successfully", response);
    }

    @GetMapping
    public ApiResponse<List<UserResponseDTO>> getUsers() {
        List<UserResponseDTO> users = userService.getUsers();
        return ApiResponse.success("Users retrieved successfully", users);
    }

    @GetMapping("/email")
    public ApiResponse<UserResponseDTO> getUserByEmail(@RequestParam String email) {
        User user = userService.getUser(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        UserResponseDTO response = userService.convertToDTO(user);
        return ApiResponse.success("User retrieved successfully", response);
    }
}