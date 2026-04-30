package com.example.project3api.config;

import com.example.project3api.service.CustomOAuth2UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.util.StringUtils;

@Configuration
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final String googleClientId;
    private final String googleClientSecret;
    private final String githubClientId;
    private final String githubClientSecret;

    public SecurityConfig(
        CustomOAuth2UserService customOAuth2UserService,
        @Value("${spring.security.oauth2.client.registration.google.client-id:}") String googleClientId,
        @Value("${spring.security.oauth2.client.registration.google.client-secret:}") String googleClientSecret,
        @Value("${spring.security.oauth2.client.registration.github.client-id:}") String githubClientId,
        @Value("${spring.security.oauth2.client.registration.github.client-secret:}") String githubClientSecret
    ) {
        this.customOAuth2UserService = customOAuth2UserService;
        this.googleClientId = googleClientId;
        this.googleClientSecret = googleClientSecret;
        this.githubClientId = githubClientId;
        this.githubClientSecret = githubClientSecret;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        HttpSecurity security = http
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/error", "/h2-console/**", "/api/auth/login", "/api/status/**").permitAll()
                .requestMatchers("/api/auth/me").authenticated()
                .requestMatchers("/api/chat/**").authenticated()
                .anyRequest().permitAll()
            )
            .logout(logout -> logout.logoutSuccessUrl("/"))
            .httpBasic(Customizer.withDefaults());

        if (isOauthConfigured()) {
            security.oauth2Login(oauth -> oauth
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(customOAuth2UserService)
                )
                .defaultSuccessUrl("/api/auth/me", true)
            );
        }

        return security.build();
    }

    private boolean isOauthConfigured() {
        return StringUtils.hasText(googleClientId)
            && StringUtils.hasText(googleClientSecret)
            && StringUtils.hasText(githubClientId)
            && StringUtils.hasText(githubClientSecret);
    }
}
