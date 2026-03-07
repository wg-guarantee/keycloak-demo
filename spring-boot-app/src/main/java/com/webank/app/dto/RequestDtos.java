package com.webank.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {
    private String username;
    private String email;
    private String password;
    private String phone;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class LoginRequest {
    private String username;
    private String password;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class ActivateRequest {
    private String email;
    private String token;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class ChangePasswordRequest {
    private String oldPassword;
    private String newPassword;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class SendSmsRequest {
    private String phone;
    private String type; // VERIFICATION, OTP
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class UserDto {
    private Long id;
    private String username;
    private String email;
    private String phone;
    private Boolean emailVerified;
    private Boolean phoneVerified;
    private Boolean accountActivated;
    private LocalDateTime createdAt;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class LoginResponse {
    private String accessToken;
    private String tokenType;
    private Long expiresIn;
    private String refreshToken;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class ApiResponse<T> {
    private int code;
    private String message;
    private T data;
}
