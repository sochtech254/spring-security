package com.sochtech.loginandsignup.controllers;

import com.sochtech.loginandsignup.dtos.AuthResponse;
import com.sochtech.loginandsignup.dtos.LoginRequest;
import com.sochtech.loginandsignup.dtos.RegisterRequest;
import com.sochtech.loginandsignup.dtos.VerifyCodeDto;
import com.sochtech.loginandsignup.services.AuthService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest registerRequest) throws MessagingException {
        AuthResponse authResponse = authService.signup(registerRequest);
        log.info("authresponse = {}", authResponse);
        if (authResponse == null) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        return ResponseEntity.ok(authService.login(loginRequest, response));
    }

    @PostMapping("/verify-code")
    public ResponseEntity<AuthResponse> verifyCode(@Valid @RequestBody VerifyCodeDto verifyCodeDto) {
        String email = verifyCodeDto.email();
        String verifyCode = verifyCodeDto.verifyCode();
        return ResponseEntity.ok(authService.verifyCode(email, verifyCode));
    }
}
