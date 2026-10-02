package com.sochtech.loginandsignup.services.impl;

import com.sochtech.loginandsignup.configuration.EmailUtils;
import com.sochtech.loginandsignup.configuration.jwt.JwtUtils;
import com.sochtech.loginandsignup.dtos.*;
import com.sochtech.loginandsignup.repos.UserRepo;
import com.sochtech.loginandsignup.services.AuthService;
import com.sochtech.loginandsignup.entities.User;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepo  userRepo;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final EmailUtils emailUtils;

    public AuthServiceImpl(UserRepo userRepo, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtUtils jwtUtils, EmailUtils emailUtils) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.emailUtils = emailUtils;
    }

    @Override
    public AuthResponse signup(RegisterRequest registerRequest) throws MessagingException {
    /*
    * 1. Validate register request data -> controller
    * 2. check if the user already exists with the given email & is verified -> error response
    * 3. user exists with given email, not verified -> check if verify code has not expired -> error response
    * 4. user exists with given email, not verified -> check if verify code has expired -> proceed with register user
    * 5. send mail to user with verification code, and link to verify code
    * */

        Optional<User> optionalUser = userRepo.findByEmail(registerRequest.email());
        User user;
        if (optionalUser.isPresent()) {
            user = optionalUser.get();
            if (user.getIsVerified()) {
                return AuthResponse.builder()
                        .message("Email already exists")
                        .success(false)
                        .build();
            } else if (user.getVerifyCodeExpiry().after(new Date())) {
                return AuthResponse.builder()
                        .message("Email already exists")
                        .success(false)
                        .build();
            }

            int verificationCode = (int)((Math.random()+1) * 10000);

            user.setUsername(registerRequest.username());
            user.setEmail(registerRequest.email());
            user.setPassword(passwordEncoder.encode(registerRequest.password()));
            user.setVerifyCode(String.valueOf(verificationCode));
            user.setVerifyCodeExpiry(new Date(System.currentTimeMillis() + 36000000)); // Verify code expires in 1 hour
            user.setRole("USER");
            user.setIsVerified(false);

            User savedUser = userRepo.save(user);

            // send mail
            final String subject = "Verify your account";
            final String EMAIL_TEMPLATE = """
                    <html>
                        <body>
                            <h1>Welcome!</h1>
                            <p>You have successfully registered to our application.</p>
                            <p>Please click on the below link to verify account:</p>
                            <a href="http://localhst:5173/verify">Verify Email</a>
                            <p>This link will expire in 30 minutes.</p>
                        </body>
                    </html>
                    """;

            emailUtils.sendMail(new MailBody(savedUser.getEmail(), subject, EMAIL_TEMPLATE));

            return AuthResponse.builder()
                    .message("User registered successfully")
                    .success(true)
                    .build();
        }

        int verificationCode = (int)((Math.random()+1) * 100000);
        User newUser = User.builder()
                .username(registerRequest.username())
                .email(registerRequest.email())
                .role("USER")
                .isVerified(false)
                .password(passwordEncoder.encode(registerRequest.password()))
                .verifyCode(String.valueOf(verificationCode))
                .verifyCodeExpiry(new Date(System.currentTimeMillis() + 3600000))
                .build();

        User savedUser = userRepo.save(newUser);

        // send mail
        final String subject = "Verify your account";
        final String EMAIL_TEMPLATE = """
                    <html>
                        <body>
                            <h1>Welcome!</h1>
                            <p>You have successfully registered to our application.</p>
                            <p>Please click on the below link to verify account:</p>
                            <a href="http://localhst:5173/verify">Verify Email</a>
                            <p>This link will expire in 30 minutes.</p>
                        </body>
                    </html>
                    """;

        emailUtils.sendMail(new MailBody(savedUser.getEmail(), subject, EMAIL_TEMPLATE));

        return AuthResponse.builder()
                .message("User registered successfully")
                .success(true)
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest loginRequest, HttpServletResponse response) {

    /*
     * 1. Validate login request data -> controller
     * 2. check if the user already exists with the given email & is verified -> login success
    * */

        Optional<User> optionalUser = userRepo.findByEmail(loginRequest.email());
        User user;
        if (optionalUser.isEmpty()) {
            return AuthResponse.builder()
                    .message("Username not found")
                    .success(false)
                    .build();
        }
        user = optionalUser.get();

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.email(), loginRequest.password()
                )
        );

        if (user.getIsVerified() && authentication.isAuthenticated()) {
            Map<String, Object> claims = new HashMap<>();
            claims.put("email", user.getEmail());
            claims.put("username", user.getUsername());

            String accessToken = jwtUtils.generateToken(claims, user, response, Token.ACCESS);
            String refreshToken = jwtUtils.generateToken(claims, user, response, Token.ACCESS);

            user.setRefreshToken(refreshToken);

            User savedUser = userRepo.save(user);

            return AuthResponse.builder()
                    .username(savedUser.getUsername())
                    .email(savedUser.getEmail())
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .isVerified(Boolean.TRUE)
                    .success(Boolean.TRUE)
                    .role(savedUser.getRole())
                    .message("Sign In successful")
                    .build();
        }


        return AuthResponse.builder()
                .message("Username not authenticated")
                .success(false)
                .build();
    }

    @Override
    public AuthResponse verifyCode(String email, String verifyCode) {
        Optional<User> optionalUser = userRepo.findByEmail(email);
        User user;
        if (optionalUser.isEmpty()) {
            return AuthResponse.builder()
                    .message("User Not Found!")
                    .success(false)
                    .build();
        }

        user = optionalUser.get();
        if (user.getVerifyCode().equals(verifyCode)) {
            if (user.getVerifyCodeExpiry().after(new Date())) {
                user.setIsVerified(true);
                user.setVerifyCode(null);
                user.setVerifyCodeExpiry(null);
                userRepo.save(user);
                return AuthResponse.builder()
                        .message("User verified successfully")
                        .success(true)
                        .build();
            } else {
                return AuthResponse.builder()
                        .message("Verification code expired! Please sign up again to generate new code.")
                        .success(true)
                        .build();
            }
        } else {
            return AuthResponse.builder()
                    .message("Verification code is invalid!")
                    .success(false)
                    .build();
        }
    }
}
