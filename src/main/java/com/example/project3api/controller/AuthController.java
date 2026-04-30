package com.example.project3api.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final String googleClientId;
    private final String googleClientSecret;
    private final String githubClientId;
    private final String githubClientSecret;

    public AuthController(
        @Value("${spring.security.oauth2.client.registration.google.client-id:}") String googleClientId,
        @Value("${spring.security.oauth2.client.registration.google.client-secret:}") String googleClientSecret,
        @Value("${spring.security.oauth2.client.registration.github.client-id:}") String githubClientId,
        @Value("${spring.security.oauth2.client.registration.github.client-secret:}") String githubClientSecret
    ) {
        this.googleClientId = googleClientId;
        this.googleClientSecret = googleClientSecret;
        this.githubClientId = githubClientId;
        this.githubClientSecret = githubClientSecret;
    }

    @GetMapping("/login")
    public Map<String, Object> loginLinks() {
        Map<String, Object> links = new LinkedHashMap<>();
        links.put("oauthEnabled", isOauthConfigured());
        if (!isOauthConfigured()) {
            links.put("message", "OAuth is disabled until Google and GitHub client credentials are configured.");
            return links;
        }

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

    private boolean isOauthConfigured() {
        return StringUtils.hasText(googleClientId)
            && StringUtils.hasText(googleClientSecret)
            && StringUtils.hasText(githubClientId)
            && StringUtils.hasText(githubClientSecret);
    }
}