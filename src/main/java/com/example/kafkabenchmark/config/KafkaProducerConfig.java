package com.example.kafkabenchmark.config;

import com.example.kafkabenchmark.model.BenchmarkMessage;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

@Configuration
public class KafkaProducerConfig {

  @Value("${spring.kafka.bootstrap-servers}")
  private String bootstrapServers;

  @Bean
  public ProducerFactory<String, BenchmarkMessage> benchmarkMessageProducerFactory() {
    Map<String, Object> configProps = new HashMap<>();
    configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
    // Optional: Add linger.ms and batch.size for producer performance tuning, but for benchmark focus on consumer.
    // configProps.put(ProducerConfig.LINGER_MS_CONFIG, "20");
    // configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, Integer.toString(16384 * 4)); // 64KB
    // configProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
    return new DefaultKafkaProducerFactory<>(configProps);
  }

  @Bean
  public KafkaTemplate<String, BenchmarkMessage> benchmarkMessageKafkaTemplate() {
    return new KafkaTemplate<>(benchmarkMessageProducerFactory());
  }
}

