package com.raghu.queue_system.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserRequestDTO {

    @NotBlank(message = "Name is required")
    private String name;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    @Size(min = 4, message = "Password must be at least 4 characters")
    private String password;

    // getters & setters

    public String getEmail() {
        return email;
    }

    public String getName() {
        // TODO Auto-generated method stub
        return name;
    }

    public String getPassword() {
        // TODO Auto-generated method stub
       return password;
    }
}