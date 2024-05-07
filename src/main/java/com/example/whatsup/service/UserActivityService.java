
package com.example.whatsup.service;

import com.example.whatsup.dto.UserActivity;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

@Service
@Slf4j
public class UserActivityService {

    @Autowired
    private KafkaAdmin kafkaAdmin;

    @Autowired
    private KafkaTemplate<String, UserActivity> kafkaTemplate;

    @Autowired
    private KafkaConsumer<String, UserActivity> consumer;

    private final Cache<String, UserActivity> userActivity;

    public UserActivityService() {
        this.userActivity =
                Caffeine
                        .newBuilder()
                        .maximumSize(10_000)
                        .expireAfterWrite(Duration.ofMinutes(10))
                        .recordStats()
                        .build();
    }

    public void invalidate(String key) {
        System.out.println("invalidating cache key: " + key);
        this.userActivity.invalidate(key);
    }

    public Optional<UserActivity> getValue(String key) {
        log.debug("Cache Request for Role: " + key);
        return Optional.ofNullable(this.userActivity.getIfPresent(key));
    }

    public void addValue(UserActivity  userActivity) {
        this.userActivity.put(userActivity.getUsername(), userActivity);
    }

    public void trackUser(String username) {
        UserActivity userActivity = new UserActivity(username, LocalDateTime.now());
        Optional<UserActivity> activity = getValue(username);
        if (activity.isEmpty()) {
            addValue(userActivity);
        }

        if (!userActivityExists(userActivity)) {
            kafkaTemplate.send("user-activity", userActivity);
        }
    }

    private boolean userActivityExists(UserActivity userActivity) {

        consumer.subscribe(Collections.singletonList("user-activity"));


        // Poll for records from Kafka
        ConsumerRecords<String, UserActivity> records = consumer.poll(Duration.ofMillis(100));

        // Check if the user activity already exists in the Kafka records
        for (ConsumerRecord<String, UserActivity> record : records) {
            UserActivity existingActivity = record.value();
            if (existingActivity.equals(userActivity)) {
                return true;
            }
        }

        // Create the topic if it doesn't exist
        if (!checkKafkaTopicExists()) {
            createKafkaTopic();
        }

        Optional<UserActivity> activity = getValue(userActivity.getUsername());

        if(activity.isPresent()) return true;

        return false;
    }

    private boolean checkKafkaTopicExists() {
        return kafkaAdmin.getConfigurationProperties().containsKey("spring.cloud.stream.kafka.bindings.input.destination.user-activity");
    }

    public void createKafkaTopic() {
        kafkaAdmin.createOrModifyTopics(TopicBuilder.name("user-activity").build());
    }
}
