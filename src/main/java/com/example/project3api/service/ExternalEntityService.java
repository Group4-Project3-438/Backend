package com.example.project3api.service;

import com.example.project3api.exception.ExternalApiException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

@Service
public class ExternalEntityService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${external.api.base-url:http://localhost:3000/api}")
    private String externalApiBaseUrl;

    public String fetchEntityPayload(String entityId) {
        String url = externalApiBaseUrl + "/entities/" + entityId;

        ResponseEntity<Object> response;
        try {
            response = restTemplate.getForEntity(url, Object.class);
        } catch (RestClientResponseException e) {
            throw new ExternalApiException(
                "External API returned an error for entityId " + entityId,
                e.getRawStatusCode(),
                e
            );
        } catch (RestClientException e) {
            throw new ExternalApiException(
                "Failed to call external API for entityId " + entityId,
                e
            );
        }

        Object body = response.getBody();
        if (body == null) {
            throw new ExternalApiException(
                "External API returned an empty payload for entityId " + entityId,
                502,
                null
            );
        }

        try {
            return objectMapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new ExternalApiException("Failed to serialize external payload", e);
        }
    }
}
