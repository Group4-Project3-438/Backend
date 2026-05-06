package com.example.project3api.controller;

import com.example.project3api.model.User;
import com.example.project3api.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    public static final String POST_OAUTH_REDIRECT_ATTR = "post_oauth_redirect_uri";
    private final UserService userService;
    private final String googleClientId;
    private final String googleClientSecret;
    private final String githubClientId;
    private final String githubClientSecret;

    public AuthController(
        UserService userService,
        @Value("${spring.security.oauth2.client.registration.google.client-id:}") String googleClientId,
        @Value("${spring.security.oauth2.client.registration.google.client-secret:}") String googleClientSecret,
        @Value("${spring.security.oauth2.client.registration.github.client-id:}") String githubClientId,
        @Value("${spring.security.oauth2.client.registration.github.client-secret:}") String githubClientSecret
    ) {
        this.userService = userService;
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

        links.put("google", "https://cardfetcherapi.onrender.com/oauth2/authorization/google");
        links.put("github", "https://cardfetcherapi.onrender.com/oauth2/authorization/github");
        return links;
    }

    @GetMapping("/oauth2/start/{provider}")
    public void startOauthLogin(
        @PathVariable String provider,
        @RequestParam(required = false) String redirectUri,
        HttpServletRequest request,
        HttpServletResponse response
    ) throws IOException {
        if (!"google".equals(provider) && !"github".equals(provider)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unsupported OAuth provider");
            return;
        }

        String normalizedRedirect = normalizeRedirectUri(redirectUri);
        if (normalizedRedirect != null) {
            request.getSession(true).setAttribute(POST_OAUTH_REDIRECT_ATTR, normalizedRedirect);
        } else {
            request.getSession(true).removeAttribute(POST_OAUTH_REDIRECT_ATTR);
        }

        response.sendRedirect("/oauth2/authorization/" + provider);
    }

    @GetMapping("/me")
    public Map<String, Object> currentUser(
        @AuthenticationPrincipal OAuth2User principal,
        OAuth2AuthenticationToken authentication
    ) {
        Map<String, Object> response = new LinkedHashMap<>();

        if (principal == null) {
            response.put("authenticated", false);
            return response;
        }

        User persistedUser = upsertAuthenticatedUser(principal, authentication);
        String userId = resolveUserId(persistedUser);

        response.put("authenticated", true);
        response.put("name", principal.getAttribute("name"));
        response.put("email", principal.getAttribute("email"));
        response.put("userId", userId);
        response.put("attributes", principal.getAttributes());

        return response;
    }

    private User upsertAuthenticatedUser(OAuth2User principal, OAuth2AuthenticationToken authentication) {
        String provider = authentication != null ? authentication.getAuthorizedClientRegistrationId() : "oauth";
        String providerId = extractProviderId(provider, principal);
        String email = extractEmail(principal);
        String name = extractName(principal);

        Optional<User> existingUser = Optional.empty();
        if (StringUtils.hasText(email)) {
            existingUser = userService.findByEmail(email.trim());
        }
        if (existingUser.isEmpty() && StringUtils.hasText(providerId)) {
            existingUser = userService.findByProviderAndProviderId(provider, providerId.trim());
        }

        User user = existingUser.orElseGet(User::new);
        user.setEmail(StringUtils.hasText(email) ? email.trim() : null);
        user.setName(name);
        user.setProvider(provider);
        user.setProviderId(StringUtils.hasText(providerId) ? providerId.trim() : null);
        return userService.save(user);
    }

    private String resolveUserId(User user) {
        if (user != null) {
            if (StringUtils.hasText(user.getEmail())) {
                return user.getEmail().trim();
            }
            if (StringUtils.hasText(user.getProvider()) && StringUtils.hasText(user.getProviderId())) {
                return user.getProvider().trim() + ":" + user.getProviderId().trim();
            }
        }
        return null;
    }

    private String normalizeRedirectUri(String redirectUri) {
        if (!StringUtils.hasText(redirectUri)) {
            return null;
        }

        String candidate = redirectUri.trim();
        try {
            URI parsed = new URI(candidate);
            if (!parsed.isAbsolute()) {
                return null;
            }
            String scheme = parsed.getScheme();
            if (!StringUtils.hasText(scheme)) {
                return null;
            }
            return candidate;
        } catch (URISyntaxException ignored) {
            return null;
        }
    }

    private String extractEmail(OAuth2User principal) {
        Object email = principal.getAttribute("email");
        if (email != null && StringUtils.hasText(email.toString())) {
            return email.toString();
        }
        return null;
    }

    private String extractName(OAuth2User principal) {
        Object name = principal.getAttribute("name");
        if (name != null && StringUtils.hasText(name.toString())) {
            return name.toString();
        }
        Object login = principal.getAttribute("login");
        if (login != null && StringUtils.hasText(login.toString())) {
            return login.toString();
        }
        return "OAuth User";
    }

    private String extractProviderId(String provider, OAuth2User principal) {
        if ("google".equals(provider)) {
            Object sub = principal.getAttribute("sub");
            return sub != null ? sub.toString() : null;
        }
        Object id = principal.getAttribute("id");
        if (id != null) {
            return id.toString();
        }
        return null;
    }

    private boolean isOauthConfigured() {
        return StringUtils.hasText(googleClientId)
            && StringUtils.hasText(googleClientSecret)
            && StringUtils.hasText(githubClientId)
            && StringUtils.hasText(githubClientSecret);
    }
}