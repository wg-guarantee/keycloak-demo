package com.webank.app.controller;

import com.webank.app.dto.*;
import com.webank.app.service.AuthService;
import com.webank.app.entity.User;
import com.webank.app.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@Slf4j
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    private final UserService userService;
    
    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserDto>> register(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = authService.register(
                request.getUsername(),
                request.getEmail(),
                request.getPassword(),
                request.getPhone()
            );
            
            UserDto userDto = convertToDto(user);
            ApiResponse<UserDto> response = ApiResponse.<UserDto>builder()
                .code(201)
                .message("注册成功，请检查邮箱进行激活")
                .data(userDto)
                .build();
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            ApiResponse<UserDto> response = ApiResponse.<UserDto>builder()
                .code(409)
                .message(e.getMessage())
                .build();
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        } catch (Exception e) {
            log.error("注册失败", e);
            ApiResponse<UserDto> response = ApiResponse.<UserDto>builder()
                .code(500)
                .message("注册失败")
                .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 激活账户
     */
    @PostMapping("/activate")
    public ResponseEntity<ApiResponse<Map<String, Object>>> activate(@Valid @RequestBody ActivateRequest request) {
        try {
            authService.activate(request.getEmail(), request.getToken());
            
            Map<String, Object> data = new HashMap<>();
            data.put("message", "账户激活成功");
            data.put("accountActivated", true);
            
            ApiResponse<Map<String, Object>> response = ApiResponse.<Map<String, Object>>builder()
                .code(200)
                .message("Success")
                .data(data)
                .build();
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("激活失败: {}", e.getMessage());
            ApiResponse<Map<String, Object>> response = ApiResponse.<Map<String, Object>>builder()
                .code(400)
                .message(e.getMessage())
                .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            log.error("激活失败", e);
            ApiResponse<Map<String, Object>> response = ApiResponse.<Map<String, Object>>builder()
                .code(500)
                .message("激活失败")
                .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 登录（这里仅作为 API 示例，实际登录由 Keycloak OAuth2 处理）
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        try {
            // 验证用户存在
            User user = userService.getUserByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
            
            if (!user.getAccountActivated()) {
                throw new IllegalArgumentException("账户未激活，请检查邮箱");
            }
            
            // 实际的密码验证和 Token 生成由 Keycloak OAuth2 处理
            // 这里仅返回示例响应
            LoginResponse loginResponse = LoginResponse.builder()
                .accessToken("eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...")
                .tokenType("Bearer")
                .expiresIn(3600L)
                .build();
            
            ApiResponse<LoginResponse> response = ApiResponse.<LoginResponse>builder()
                .code(200)
                .message("登录成功")
                .data(loginResponse)
                .build();
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            ApiResponse<LoginResponse> response = ApiResponse.<LoginResponse>builder()
                .code(401)
                .message(e.getMessage())
                .build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (Exception e) {
            log.error("登录失败", e);
            ApiResponse<LoginResponse> response = ApiResponse.<LoginResponse>builder()
                .code(500)
                .message("登录失败")
                .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 发送短信验证码
     */
    @PostMapping("/send-sms")
    public ResponseEntity<ApiResponse<Map<String, Object>>> sendSms(@Valid @RequestBody SendSmsRequest request) {
        try {
            authService.sendSmsVerificationCode(request.getPhone());
            
            Map<String, Object> data = new HashMap<>();
            data.put("message", "验证码已发送");
            data.put("expiresIn", 300); // 5 minutes
            
            ApiResponse<Map<String, Object>> response = ApiResponse.<Map<String, Object>>builder()
                .code(200)
                .message("Success")
                .data(data)
                .build();
            
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            log.warn("短信发送频率限制: {}", e.getMessage());
            ApiResponse<Map<String, Object>> response = ApiResponse.<Map<String, Object>>builder()
                .code(429)
                .message(e.getMessage())
                .build();
            return ResponseEntity.status(429).body(response);
        } catch (Exception e) {
            log.error("短信发送失败", e);
            ApiResponse<Map<String, Object>> response = ApiResponse.<Map<String, Object>>builder()
                .code(500)
                .message("短信发送失败")
                .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 发送密码重置邮件
     */
    @PostMapping("/send-password-reset-email")
    public ResponseEntity<ApiResponse<Map<String, Object>>> sendPasswordResetEmail(@RequestParam String email) {
        try {
            authService.sendPasswordResetEmail(email);
            
            Map<String, Object> data = new HashMap<>();
            data.put("message", "重置密码邮件已发送");
            
            ApiResponse<Map<String, Object>> response = ApiResponse.<Map<String, Object>>builder()
                .code(200)
                .message("Success")
                .data(data)
                .build();
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("邮件发送失败: {}", e.getMessage());
            ApiResponse<Map<String, Object>> response = ApiResponse.<Map<String, Object>>builder()
                .code(404)
                .message(e.getMessage())
                .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            log.error("邮件发送失败", e);
            ApiResponse<Map<String, Object>> response = ApiResponse.<Map<String, Object>>builder()
                .code(500)
                .message("邮件发送失败")
                .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    private UserDto convertToDto(User user) {
        return UserDto.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .phone(user.getPhone())
            .emailVerified(user.getEmailVerified())
            .phoneVerified(user.getPhoneVerified())
            .accountActivated(user.getAccountActivated())
            .createdAt(user.getCreatedAt())
            .build();
    }
}
