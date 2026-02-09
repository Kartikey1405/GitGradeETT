//package com.gitgrade.GitGradeSpring.service;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.gitgrade.GitGradeSpring.entity.User;
//import com.gitgrade.GitGradeSpring.repository.UserRepository;
//import com.gitgrade.GitGradeSpring.util.JwtUtil;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.web.reactive.function.BodyInserters;
//import org.springframework.web.reactive.function.client.WebClient;
//
//import java.util.Optional;
//
//@Service
//public class AuthService {
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private JwtUtil jwtUtil;
//
//    // Inject values from application.properties
//    @Value("${spring.security.oauth2.client.registration.google.client-id}")
//    private String clientId;
//
//    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
//    private String clientSecret;
//
//    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
//    private String redirectUri;
//
//    private final WebClient webClient = WebClient.create();
//
//    public String processGoogleLogin(String code) {
//        // 1. Manually swap 'code' for 'access_token' from Google
//        JsonNode tokenResponse = webClient.post()
//                .uri("https://oauth2.googleapis.com/token")
//                .body(BodyInserters.fromFormData("code", code)
//                        .with("client_id", clientId)
//                        .with("client_secret", clientSecret)
//                        .with("redirect_uri", "http://localhost:8080/login/oauth2/code/google") // Must match console
//                        .with("grant_type", "authorization_code"))
//                .retrieve()
//                .bodyToMono(JsonNode.class)
//                .block(); // Blocking here is okay for simple login flow
//
//        String googleAccessToken = tokenResponse.get("access_token").asText();
//
//        // 2. Use 'access_token' to get User Info
//        JsonNode userInfo = webClient.get()
//                .uri("https://www.googleapis.com/oauth2/v2/userinfo")
//                .headers(h -> h.setBearerAuth(googleAccessToken))
//                .retrieve()
//                .bodyToMono(JsonNode.class)
//                .block();
//
//        String email = userInfo.get("email").asText();
//        String name = userInfo.has("name") ? userInfo.get("name").asText() : "Unknown User";
//
//        // 3. Save or Update User in DB
//        User user = userRepository.findByEmail(email).orElseGet(() -> {
//            User newUser = new User();
//            newUser.setEmail(email);
//            newUser.setFullName(name);
//            return userRepository.save(newUser);
//        });
//
//        // 4. Generate our own JWT
//        return jwtUtil.generateToken(user.getEmail(), user.getId());
//    }
//}




package com.gitgrade.GitGradeSpring.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.gitgrade.GitGradeSpring.entity.User;
import com.gitgrade.GitGradeSpring.repository.UserRepository;
import com.gitgrade.GitGradeSpring.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    // Inject values from application.properties
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    // ✅ CHANGED: Inject the custom redirect URI that matches your React Frontend
    @Value("${app.oauth.redirect-uri}")
    private String redirectUri;

    private final WebClient webClient = WebClient.create();

    public String processGoogleLogin(String code) {
        // 1. Manually swap 'code' for 'access_token' from Google
        JsonNode tokenResponse = webClient.post()
                .uri("https://oauth2.googleapis.com/token")
                .body(BodyInserters.fromFormData("code", code)
                        .with("client_id", clientId)
                        .with("client_secret", clientSecret)
                        .with("redirect_uri", redirectUri) // ✅ CHANGED: Uses the variable from application.properties
                        .with("grant_type", "authorization_code"))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block(); // Blocking here is okay for simple login flow

        String googleAccessToken = tokenResponse.get("access_token").asText();

        // 2. Use 'access_token' to get User Info
        JsonNode userInfo = webClient.get()
                .uri("https://www.googleapis.com/oauth2/v2/userinfo")
                .headers(h -> h.setBearerAuth(googleAccessToken))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        String email = userInfo.get("email").asText();
        String name = userInfo.has("name") ? userInfo.get("name").asText() : "Unknown User";

        // 3. Save or Update User in DB
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setFullName(name);
            return userRepository.save(newUser);
        });

        // 4. Generate our own JWT
        return jwtUtil.generateToken(user.getEmail(), user.getId());
    }
}