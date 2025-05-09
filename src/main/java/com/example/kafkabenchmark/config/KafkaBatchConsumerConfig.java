package com.example.kafkabenchmark.config;

import com.example.kafkabenchmark.model.BenchmarkMessage;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

@Configuration
public class KafkaBatchConsumerConfig {

  @Value("${spring.kafka.bootstrap-servers}")
  private String bootstrapServers;

  @Value("${kafka.topic.batch}")
  private String topicBatch;

  // ConsumerFactory bean for batch consumer
  @Bean
  public ConsumerFactory<String, BenchmarkMessage> batchConsumerFactory() {
    Map<String, Object> props = new HashMap<>();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "batch-consumer-group");
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
    props.put(JsonDeserializer.TRUSTED_PACKAGES, "*"); // Not recommended for production
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    // Batch specific properties will be set in application.properties or via listener factory
    // e.g., max.poll.records, fetch.max.wait.ms
    return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new JsonDeserializer<>(BenchmarkMessage.class, false));
  }

  // KafkaListenerContainerFactory for batch consumer
  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, BenchmarkMessage> batchKafkaListenerContainerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, BenchmarkMessage> factory = new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(batchConsumerFactory());
    factory.setBatchListener(true); // Enable batch listening
    // The batch timeout (fetch.max.wait.ms) and max.poll.records are typically configured
    // at the consumer property level (e.g., in application.properties) or directly on the ConsumerFactory.
    // Spring Boot will auto-configure these from application.properties if spring.kafka.consumer.* properties are used.
    // For explicit control here, you could set them on the factory's consumer properties:
    // factory.getContainerProperties().setConsumerProperties();
    return factory;
  }
}

