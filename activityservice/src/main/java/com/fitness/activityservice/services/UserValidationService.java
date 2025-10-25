package com.fitness.activityservice.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserValidationService {

    private final WebClient userServiceWebClient;

    public boolean validateUser (String userId) {
        try {
            return Boolean.TRUE.equals(userServiceWebClient.get()
                    .uri("api/users/{userId}/validate", userId)
                    .retrieve().bodyToMono(Boolean.class).block());
        } catch (WebClientResponseException e) {
            e.fillInStackTrace();
        }
        return false;
    }

}
