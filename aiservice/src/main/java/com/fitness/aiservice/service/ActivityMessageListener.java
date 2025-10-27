package com.fitness.aiservice.service;

import com.fitness.aiservice.model.Activity;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActivityMessageListener {

    @KafkaListener(topics = "${kafka.topic.name}", groupId = "activity-processor-group")
    public void processActivity (Activity activity) {
        if (activity == null) {
            log.warn("Received null activity - skipping (likely deserialization error)");
            return;
        }
        
        try {
            log.info("==========> Received activity from Kafka: userId={}, type={}, duration={}", 
                    activity.getUserId(), activity.getType(), activity.getDuration());
        } catch (Exception e) {
            log.error("Error processing activity", e);
            throw e;
        }
    }

}
