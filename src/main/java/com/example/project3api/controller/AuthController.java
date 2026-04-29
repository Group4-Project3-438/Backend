package com.example.project3api.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @GetMapping("/login")
    public Map<String, String> loginLinks() {
        Map<String, String> links = new LinkedHashMap<>();
        links.put("google", "http://localhost:8081/oauth2/authorization/google");
        links.put("github", "http://localhost:8081/oauth2/authorization/github");
        return links;
    }

    @GetMapping("/me")
    public Map<String, Object> currentUser(@AuthenticationPrincipal OAuth2User principal) {
        Map<String, Object> response = new LinkedHashMap<>();

        if (principal == null) {
            response.put("authenticated", false);
            return response;
        }

        response.put("authenticated", true);
        response.put("name", principal.getAttribute("name"));
        response.put("email", principal.getAttribute("email"));
        response.put("attributes", principal.getAttributes());

        return response;
    }
}