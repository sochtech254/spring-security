package com.sochtech.loginandsignup.dtos;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public final class AuthResponse {
    private String username;
    private String email;
    private String accessToken;
    private String refreshToken;
    private Boolean isVerified;
    private String role;
    private Boolean success;
    private String message;
}
