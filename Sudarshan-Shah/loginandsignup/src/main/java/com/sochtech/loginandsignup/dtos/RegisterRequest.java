package com.sochtech.loginandsignup.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(@NotBlank(message = "Username is required") String username,
                              @NotBlank(message = "Email is required")
                              @Email(message = "Please provide valid email") String email,
                              @NotBlank(message = "Password is required") String password) {
}
