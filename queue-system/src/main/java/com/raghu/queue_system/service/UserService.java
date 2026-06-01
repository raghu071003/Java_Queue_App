package com.raghu.queue_system.service;

import com.raghu.queue_system.dto.UserResponseDTO;
import com.raghu.queue_system.model.User;
import com.raghu.queue_system.model.UserRole;
import com.raghu.queue_system.model.Doctor;
import com.raghu.queue_system.repository.UserRepository;
import com.raghu.queue_system.repository.DoctorRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, DoctorRepository doctorRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.doctorRepository = doctorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User saveUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public List<UserResponseDTO> getUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .toList();
    }

    public Optional<User> getUser(String email) {
        return userRepository.findByEmail(email);
    }

    public UserResponseDTO convertToDTO(User user) {
        Long doctorId = null;
        if (user.getRole() == UserRole.DOCTOR) {
            Optional<Doctor> matchingDoctor = doctorRepository.findByEmail(user.getEmail());
            if (matchingDoctor.isPresent()) {
                doctorId = matchingDoctor.get().getId();
            }
        }

        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                doctorId);
    }

    public User authenticate(String email, String password) {
        User user = userRepository.findByEmail(email)
                        .orElseThrow(() -> new com.raghu.queue_system.exception.UserNotFoundException("User not found"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new com.raghu.queue_system.exception.InvalidPasswordException("Invalid Password");
        }
        return user;
    }
}