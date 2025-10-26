package com.fitness.aiservice.service;

import com.fitness.aiservice.model.Activity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActivityMessageListener {

    @KafkaListener(topics = "activity-events", groupId = "activity-processor-group-v3")
    public void processActivity (Activity activity) {
        log.info("Received activity for processing : {}", activity.getUserId());

        System.out.println("processing ................ done");
    }

}
