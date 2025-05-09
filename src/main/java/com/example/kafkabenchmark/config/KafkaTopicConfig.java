package com.example.kafkabenchmark.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

  @Value("${kafka.topic.normal}")
  private String normalTopic;

  @Value("${kafka.topic.batch}")
  private String batchTopic;

  @Value("${spring.kafka.test.partitions:1}")
  private int partitions;

  @Value("${spring.kafka.test.replicas:1}")
  private int replicas;

  @Bean
  public NewTopic normalBenchmarkTopic() {
    return TopicBuilder.name(normalTopic)
        .partitions(partitions)
        .replicas(replicas)
        .build();
  }

  @Bean
  public NewTopic batchBenchmarkTopic() {
    return TopicBuilder.name(batchTopic)
        .partitions(partitions)
        .replicas(replicas)
        .build();
  }
}

