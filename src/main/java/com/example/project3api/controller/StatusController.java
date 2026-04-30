package com.example.project3api.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/status")
public class StatusController {

    private final JdbcTemplate jdbcTemplate;
    private final String datasourceUrl;
    private final String authMode;
    private final String googleClientId;
    private final String googleClientSecret;
    private final String githubClientId;
    private final String githubClientSecret;

    public StatusController(
        JdbcTemplate jdbcTemplate,
        @Value("${spring.datasource.url}") String datasourceUrl,
        @Value("${app.auth.mode:application-oauth2}") String authMode,
        @Value("${spring.security.oauth2.client.registration.google.client-id:}") String googleClientId,
        @Value("${spring.security.oauth2.client.registration.google.client-secret:}") String googleClientSecret,
        @Value("${spring.security.oauth2.client.registration.github.client-id:}") String githubClientId,
        @Value("${spring.security.oauth2.client.registration.github.client-secret:}") String githubClientSecret
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.datasourceUrl = datasourceUrl;
        this.authMode = authMode;
        this.googleClientId = googleClientId;
        this.googleClientSecret = googleClientSecret;
        this.githubClientId = githubClientId;
        this.githubClientSecret = githubClientSecret;
    }

    @GetMapping("/database")
    public ResponseEntity<Map<String, Object>> databaseStatus() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("service", "database");
        response.put("checkedAt", Instant.now().toString());
        response.put("datasource", sanitizeDatasourceUrl(datasourceUrl));

        try {
            Integer ping = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            response.put("status", "UP");
            response.put("connected", ping != null && ping == 1);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "DOWN");
            response.put("connected", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
        }
    }

    private String sanitizeDatasourceUrl(String url) {
        if (url == null || url.isBlank()) {
            return "not-configured";
        }
        return url.replaceAll("(?i)(password=)[^&]+", "$1***");
    }

    @GetMapping("/auth")
    public ResponseEntity<Map<String, Object>> authStatus() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("service", "auth");
        response.put("checkedAt", Instant.now().toString());
        response.put("authMode", authMode);
        response.put("managedByApplication", "application-oauth2".equalsIgnoreCase(authMode));
        response.put("supabaseAuthManaged", false);
        response.put("oauthConfigured", isOauthConfigured());
        response.put("providers", "google,github");
        return ResponseEntity.ok(response);
    }

    private boolean isOauthConfigured() {
        return hasText(googleClientId)
            && hasText(googleClientSecret)
            && hasText(githubClientId)
            && hasText(githubClientSecret);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
