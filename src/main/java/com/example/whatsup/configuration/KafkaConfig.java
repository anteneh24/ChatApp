
package com.example.whatsup.configuration;
import com.example.whatsup.dto.UserActivity;
import com.example.whatsup.entity.Message;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@EnableKafka
@Configuration
public class KafkaConfig {
 @Value("${spring.kafka.bootstrap-servers}")
 private String bootstrapServers;

 String groupId = "whatsup";

 @Bean
 public KafkaTemplate<String, Message> kafkaTemplate() {
  return new KafkaTemplate<>(producerFactory());
 }

 @Bean()
 public KafkaTemplate<String, UserActivity> kafkaTemplateUserActivity() {return new KafkaTemplate<>(producerFactoryUserActivity());};

 @Bean
 public ProducerFactory<String, Message> producerFactory() {
  return new DefaultKafkaProducerFactory<>(producerConfigs());
 }

 @Bean
 public ProducerFactory<String, UserActivity> producerFactoryUserActivity() {
  return new DefaultKafkaProducerFactory<>(producerConfigs());
 }

 @Bean
 public Map<String, Object> producerConfigs() {
  Map<String, Object> props = new HashMap<>();
  props.put(org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
  props.put(org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
  props.put(org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
  return props;
 }

 @Bean
 public ConsumerFactory<String, Message> consumerFactory() {
  return new DefaultKafkaConsumerFactory<>(consumerConfigs(), new StringDeserializer(), new JsonDeserializer<>(Message.class));
 }

 @Bean
 public Map<String, Object> consumerConfigs() {
  Map<String, Object> props = new HashMap<>();
  props.put(org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
  props.put(org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG, this.groupId);
  props.put(org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
  props.put(org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
  return props;
 }

 @Bean
 public ConcurrentKafkaListenerContainerFactory<String, Message> kafkaListenerContainerFactory() {
  ConcurrentKafkaListenerContainerFactory<String, Message> factory = new ConcurrentKafkaListenerContainerFactory<>();
  factory.setConsumerFactory(consumerFactory());
  factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
  return factory;
 }

 @Bean
 public KafkaConsumer<String, Message> kafkaConsumer() {
  Properties properties = new Properties();
  properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
  properties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
  properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
  properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class.getName());
  KafkaConsumer<String, Message> kafkaConsumer = new KafkaConsumer<>(properties);
  return kafkaConsumer;
 }

 @Bean
 public KafkaConsumer<String, UserActivity> kafkaConsumerUserActivity() {
  Properties properties = new Properties();
  properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
  properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "user-activity-consumer-group");
  properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
  properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class.getName());
  KafkaConsumer<String, UserActivity> kafkaConsumer = new KafkaConsumer<>(properties);
  return kafkaConsumer;
 }

 @Bean
 public KafkaAdmin kafkaAdmin() {
  Map<String, Object> configs = new HashMap<>();
  configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
  configs.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000);
  return new KafkaAdmin(configs);
 }
}
