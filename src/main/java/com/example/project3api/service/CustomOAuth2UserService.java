package com.example.project3api.service;

import com.example.project3api.model.User;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserService userService;

    public CustomOAuth2UserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
        OAuth2User oauth2User = delegate.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oauth2User.getAttributes();

        String email = extractEmail(registrationId, attributes);
        String name = extractName(registrationId, attributes);
        String providerId = extractProviderId(registrationId, attributes);

        if (email != null) {
            Optional<User> existingUser = userService.findByEmail(email);

            User user = existingUser.orElseGet(User::new);
            user.setEmail(email);
            user.setName(name);
            user.setProvider(registrationId);
            user.setProviderId(providerId);

            userService.save(user);
        }

        return oauth2User;
    }

    private String extractEmail(String registrationId, Map<String, Object> attributes) {
        if ("google".equals(registrationId)) {
            return (String) attributes.get("email");
        }

        if ("github".equals(registrationId)) {
            Object email = attributes.get("email");
            return email != null ? email.toString() : null;
        }

        return null;
    }

    private String extractName(String registrationId, Map<String, Object> attributes) {
        if ("google".equals(registrationId)) {
            return (String) attributes.get("name");
        }

        if ("github".equals(registrationId)) {
            Object name = attributes.get("name");
            if (name != null && !name.toString().isBlank()) {
                return name.toString();
            }
            Object login = attributes.get("login");
            return login != null ? login.toString() : "GitHub User";
        }

        return "OAuth User";
    }

    private String extractProviderId(String registrationId, Map<String, Object> attributes) {
        if ("google".equals(registrationId)) {
            Object sub = attributes.get("sub");
            return sub != null ? sub.toString() : null;
        }

        if ("github".equals(registrationId)) {
            Object id = attributes.get("id");
            return id != null ? id.toString() : null;
        }

        return null;
    }
}