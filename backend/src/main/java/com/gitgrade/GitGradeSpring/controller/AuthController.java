package com.gitgrade.GitGradeSpring.controller;

import com.gitgrade.GitGradeSpring.service.AuthService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    // DTO for incoming JSON
    @Data
    public static class LoginRequest {
        private String code;
    }

    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestBody LoginRequest request) {
        try {
            String jwtToken = authService.processGoogleLogin(request.getCode());

            // Return exactly what Python returned
            Map<String, String> response = new HashMap<>();
            response.put("access_token", jwtToken);
            response.put("token_type", "bearer");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Login Failed: " + e.getMessage());
        }
    }
}