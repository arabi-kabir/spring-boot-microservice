package com.fitness.aiservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.aiservice.model.Activity;
import com.fitness.aiservice.model.Recommendation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActivityAiService {

    private final GeminiService geminiService;

    public Recommendation generateRecommendation (Activity activity){
        String prompt = createPromptForActivity (activity);
        String aiResponse = geminiService.getRecommendations(prompt);
        return processAIResponse (activity, aiResponse);
    }

    private Recommendation processAIResponse (Activity activity, String aiResponse) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(aiResponse);
            JsonNode textNode = rootNode
                    .path("candidates")
                    .get(0)
                    .path("content")
                    .get("parts")
                    .get(0)
                    .path("text");

            String jsonContent = textNode.asText()
                    .replaceAll("```json\\n", "")
                    .replaceAll("\\n", "")
                    .trim();

            log.info("JSON FROM AI {}", jsonContent);

            JsonNode analysisJson = objectMapper.readTree(jsonContent);
            JsonNode analysisNode = analysisJson.get("analysis");

            StringBuilder fullAnalysis = new StringBuilder();

            addAnalysisSection (fullAnalysis, analysisNode, "overall", "Overall:");
            addAnalysisSection (fullAnalysis, analysisNode, "pace", "Pace:");
            addAnalysisSection (fullAnalysis, analysisNode, "heartRate", "Heart Rate:");
            addAnalysisSection (fullAnalysis, analysisNode, "caloriesBurned", "Calories:");

            List<String> improvements = extractImprovements (analysisJson.path("improvements"));
            List<String> suggestions = extractSuggestions (analysisJson.path("suggestions"));
            List<String> safety = extractSafety (analysisJson.path("safety"));

            return Recommendation.builder()
                    .activityId(activity.getId())
                    .userId(activity.getUserId())
                    .type(activity.getType().toString())
                    .improvements(improvements)
                    .recommendation(fullAnalysis.toString().trim())
                    .suggestions(suggestions)
                    .safety(safety)
                    .createdAt(LocalDateTime.now())
                    .build();
        } catch (Exception e) {
            e.fillInStackTrace();
            return createDefaultRecommendation(activity);
        }
    }

    private Recommendation createDefaultRecommendation (Activity activity) {
        return Recommendation.builder()
                .activityId(activity.getId())
                .userId(activity.getUserId())
                .type(activity.getType().toString())
                .improvements(Collections.singletonList("Continue with you current routine"))
                .recommendation("Unable to generate detailed analysis")
                .suggestions(Collections.singletonList("Enjoy bro"))
                .safety(Collections.singletonList("Do or Die"))
                .createdAt(LocalDateTime.now())
                .build();
    }

    private List<String> extractSafety (JsonNode safety) {
        List<String> safetyList = new ArrayList<>();

        if (safety.isArray()) {
            safety.forEach(item -> safetyList.add(item.asText()));
        }

        return safetyList.isEmpty() ? Collections.singletonList("No specific safety provided") : safetyList;
    }

    private List<String> extractSuggestions (JsonNode suggestions) {
        List<String> suggestionList = new ArrayList<>();

        if (suggestions.isArray()) {
            suggestions.forEach(improvement -> {
                String workout = improvement.path("workout").asText();
                String description = improvement.path("description").asText();

                suggestionList.add(String.format("%s: %s", workout, description));
            });
        }

        return suggestionList.isEmpty() ? Collections.singletonList("No specific suggestion provided") : suggestionList;
    }

    private List<String> extractImprovements (JsonNode improvements) {
        List<String> improvementList = new ArrayList<>();

        if (improvements.isArray()) {
            improvements.forEach(improvement -> {
                String area = improvement.path("area").asText();
                String recommendation = improvement.path("recommendation").asText();

                improvementList.add(String.format("%s: %s", area, recommendation));
            });
        }

        return improvementList.isEmpty() ? Collections.singletonList("No specific improvements provided") : improvementList;
    }

    private void addAnalysisSection (StringBuilder fullAnalysis, JsonNode analysisNode, String key, String prefix) {
        if (!analysisNode.path(key).isMissingNode()) {
            fullAnalysis.append(prefix)
                    .append(analysisNode.path(key).asText())
                    .append("\n\n");
        }
    }

    private String createPromptForActivity (Activity activity) {
        return String.format("""
        Analyze this fitness activity and provide detailed recommendations in the following EXACT JSON format:
        {
          "analysis": {
            "overall": "Overall analysis here",
            "pace": "Pace analysis here",
            "heartRate": "Heart rate analysis here",
            "caloriesBurned": "Calories analysis here"
          },
          "improvements": [
            {
              "area": "Area name",
              "recommendation": "Detailed recommendation"
            }
          ],
          "suggestions": [
            {
              "workout": "Workout name",
              "description": "Detailed workout description"
            }
          ],
          "safety": [
            "Safety point 1",
            "Safety point 2"
          ]
        }

        Analyze this activity:
        Activity Type: %s
        Duration: %d minutes
        Calories Burned: %d
        Additional Metrics: %s
        
        Provide detailed analysis focusing on performance, improvements, next workout suggestions, and safety guidelines.
        Ensure the response follows the EXACT JSON format shown above.
        """,
                activity.getType(),
                activity.getDuration(),
                activity.getCaloriesBurned(),
                activity.getAdditionalMetrics()
        );
    }

}
