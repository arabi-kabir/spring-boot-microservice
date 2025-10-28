package com.fitness.aiservice.controller;

import com.fitness.aiservice.enums.ActivityType;
import com.fitness.aiservice.model.Activity;
import com.fitness.aiservice.model.Recommendation;
import com.fitness.aiservice.service.ActivityMessageListener;
import com.fitness.aiservice.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    private final ActivityMessageListener activityMessageListener;

    @GetMapping("/user/{userId}")
    private ResponseEntity<List<Recommendation>> getUserRecommendation (@PathVariable String userId) {
        return ResponseEntity.ok(recommendationService.getUserRecommendation(userId));
    }

    @GetMapping("/activity/{activityId}")
    private ResponseEntity<Recommendation> getUserActivityRecommendation (@PathVariable String activityId) {
        return ResponseEntity.ok(recommendationService.getUserActivityRecommendation(activityId));
    }

}
