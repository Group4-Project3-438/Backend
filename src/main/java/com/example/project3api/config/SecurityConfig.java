package com.example.project3api.config;

import com.example.project3api.controller.AuthController;
import com.example.project3api.service.CustomOAuth2UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.StringUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Configuration
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final String googleClientId;
    private final String googleClientSecret;
    private final String githubClientId;
    private final String githubClientSecret;
    private final String frontendRedirectUrl;

    public SecurityConfig(
        CustomOAuth2UserService customOAuth2UserService,
        @Value("${spring.security.oauth2.client.registration.google.client-id:}") String googleClientId,
        @Value("${spring.security.oauth2.client.registration.google.client-secret:}") String googleClientSecret,
        @Value("${spring.security.oauth2.client.registration.github.client-id:}") String githubClientId,
        @Value("${spring.security.oauth2.client.registration.github.client-secret:}") String githubClientSecret,
        @Value("${APP_FRONTEND_URL:http://localhost:8081}") String frontendRedirectUrl
    ) {
        this.customOAuth2UserService = customOAuth2UserService;
        this.googleClientId = googleClientId;
        this.googleClientSecret = googleClientSecret;
        this.githubClientId = githubClientId;
        this.githubClientSecret = githubClientSecret;
        this.frontendRedirectUrl = frontendRedirectUrl;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        HttpSecurity security = http
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/error", "/h2-console/**", "/api/auth/login", "/api/auth/oauth2/start/**", "/api/status/**").permitAll()
                .requestMatchers("/api/auth/me").authenticated()
                .requestMatchers("/api/chat/**").authenticated()
                .anyRequest().permitAll()
            )
            .exceptionHandling(ex -> ex
                .defaultAuthenticationEntryPointFor(
                    new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                    new AntPathRequestMatcher("/api/**")
                )
            )
            .logout(logout -> logout.logoutSuccessHandler((request, response, authentication) -> {
                response.setStatus(HttpStatus.OK.value());
            }));

        if (isOauthConfigured()) {
            security.oauth2Login(oauth -> oauth
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(customOAuth2UserService)
                )
                .successHandler((request, response, authentication) -> {
                    String sessionRedirect = null;
                    if (request.getSession(false) != null) {
                        Object stored = request.getSession(false).getAttribute(AuthController.POST_OAUTH_REDIRECT_ATTR);
                        if (stored instanceof String && !((String) stored).isBlank()) {
                            sessionRedirect = ((String) stored).trim();
                        }
                        request.getSession(false).removeAttribute(AuthController.POST_OAUTH_REDIRECT_ATTR);
                    }
                    String target = sessionRedirect != null ? sessionRedirect : frontendRedirectUrl;
                    response.sendRedirect(appendAuthParams(target, authentication));
                })
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

    private String appendAuthParams(String target, Authentication authentication) {
        if (!StringUtils.hasText(target)) {
            return target;
        }

        String provider = null;
        String providerId = null;
        String name = null;
        String email = null;

        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
            provider = oauthToken.getAuthorizedClientRegistrationId();
            OAuth2User oauth2User = oauthToken.getPrincipal();
            Object nameAttr = oauth2User.getAttribute("name");
            Object emailAttr = oauth2User.getAttribute("email");
            Object loginAttr = oauth2User.getAttribute("login");
            Object idAttr = oauth2User.getAttribute("id");
            Object subAttr = oauth2User.getAttribute("sub");

            if (nameAttr != null && StringUtils.hasText(nameAttr.toString())) {
                name = nameAttr.toString().trim();
            } else if (loginAttr != null && StringUtils.hasText(loginAttr.toString())) {
                name = loginAttr.toString().trim();
            }
            if (emailAttr != null && StringUtils.hasText(emailAttr.toString())) {
                email = emailAttr.toString().trim();
            }
            if (subAttr != null && StringUtils.hasText(subAttr.toString())) {
                providerId = subAttr.toString().trim();
            } else if (idAttr != null && StringUtils.hasText(idAttr.toString())) {
                providerId = idAttr.toString().trim();
            }
        }

        String userId = StringUtils.hasText(email)
            ? email
            : (StringUtils.hasText(provider) && StringUtils.hasText(providerId)
                ? provider + ":" + providerId
                : null);

        String sep = target.contains("?") ? "&" : "?";
        String redirect = target + sep + "auth=success";
        if (StringUtils.hasText(userId)) {
            redirect += "&userId=" + URLEncoder.encode(userId, StandardCharsets.UTF_8);
        }
        if (StringUtils.hasText(name)) {
            redirect += "&name=" + URLEncoder.encode(name, StandardCharsets.UTF_8);
        }
        if (StringUtils.hasText(email)) {
            redirect += "&email=" + URLEncoder.encode(email, StandardCharsets.UTF_8);
        }
        if (StringUtils.hasText(provider)) {
            redirect += "&provider=" + URLEncoder.encode(provider, StandardCharsets.UTF_8);
        }
        return redirect;
    }
}
