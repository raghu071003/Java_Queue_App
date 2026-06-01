package com.raghu.queue_system.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.raghu.queue_system.dto.ApiResponse;
import com.raghu.queue_system.dto.LoginRequest;
import com.raghu.queue_system.dto.LoginResponse;
import com.raghu.queue_system.dto.UserRequestDTO;
import com.raghu.queue_system.dto.UserResponseDTO;
import com.raghu.queue_system.model.User;
import com.raghu.queue_system.model.UserRole;
import com.raghu.queue_system.model.Doctor;
import com.raghu.queue_system.service.UserService;
import com.raghu.queue_system.repository.DoctorRepository;
import com.raghu.queue_system.util.JwtUtil;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserService userService;
    private final DoctorRepository doctorRepository;

    public AuthController(UserService userService, DoctorRepository doctorRepository) {
         this.userService = userService;
         this.doctorRepository = doctorRepository;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) {
        User user = userService.authenticate(request.getEmail(), request.getPassword());
        String token = JwtUtil.generateToken(user.getEmail());
        UserResponseDTO userDTO = userService.convertToDTO(user);
        
        LoginResponse response = new LoginResponse(token, userDTO);
        return ApiResponse.success("Login successful", response);
    }

    @PostMapping("/register")
    public ApiResponse<UserResponseDTO> createUser(@Valid @RequestBody UserRequestDTO request) {
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setRole(request.getRole() != null ? request.getRole() : UserRole.PATIENT);

        User savedUser = userService.saveUser(user);
        
        // If registering as a doctor, automatically create the Doctor entity linked by email
        if (user.getRole() == UserRole.DOCTOR) {
            Doctor doctor = new Doctor();
            doctor.setName(user.getName());
            doctor.setSpecialization(null); // Unset to prompt on first login
            doctor.setAvgServiceTime(null); // Unset to prompt on first login
            doctor.setEmail(user.getEmail());
            doctorRepository.save(doctor);
        }

        UserResponseDTO response = userService.convertToDTO(savedUser);
        return ApiResponse.success("Registration successful", response);
    }

    @GetMapping("/me")
    public ApiResponse<String> currentUser(Authentication authentication) {
        return ApiResponse.success("User fetched successfully", "Logged in User : " + authentication.getName());
    }
}
