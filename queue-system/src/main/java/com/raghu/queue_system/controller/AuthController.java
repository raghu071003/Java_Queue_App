package com.raghu.queue_system.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.raghu.queue_system.dto.LoginRequest;
import com.raghu.queue_system.dto.UserRequestDTO;
import com.raghu.queue_system.dto.UserResponseDTO;
import com.raghu.queue_system.model.User;
import com.raghu.queue_system.service.UserService;
import com.raghu.queue_system.util.JwtUtil;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
         this.userService = userService;
    }

    @PostMapping("/login")
    public Map<String,String> login(@RequestBody LoginRequest request) {
        User user = userService.authenticate(request.getEmail(), request.getPassword());
        String token = JwtUtil.generateToken(user.getEmail());

        Map<String,String> response = new HashMap<>();
        response.put("token" ,token);
        return response;
    }

    @PostMapping("/register")
     public UserResponseDTO createUser(@Valid @RequestBody UserRequestDTO request) {
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());

        User savedUser = userService.saveUser(user);
        return userService.convertToDTO(savedUser);
    }

    @GetMapping("/me")
    public String currentUser(Authentication authentication) {
        return "Logged in User : " + authentication.getName();
    }
}
