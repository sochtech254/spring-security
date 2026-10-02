package com.sochtech.loginandsignup.services;

import com.sochtech.loginandsignup.dtos.AuthResponse;
import com.sochtech.loginandsignup.dtos.LoginRequest;
import com.sochtech.loginandsignup.dtos.RegisterRequest;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
    AuthResponse login(LoginRequest loginRequest,  HttpServletResponse response);

    AuthResponse signup(RegisterRequest registerRequest) throws MessagingException;

    AuthResponse verifyCode(String email, String verifyCode);
}
