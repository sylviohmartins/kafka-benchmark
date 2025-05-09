package com.example.kafkabenchmark.consumer.normal;

import com.example.kafkabenchmark.model.BenchmarkMessage;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NormalKafkaConsumer {

  private static final Logger LOGGER = LoggerFactory.getLogger(NormalKafkaConsumer.class);
  private final AtomicLong messageCounter = new AtomicLong(0);

  @KafkaListener(topics = "${kafka.topic.normal}", groupId = "normal-consumer-group", containerFactory = "normalKafkaListenerContainerFactory")
  public void receiveMessage(BenchmarkMessage message) {
    // Simulate some processing
    try {
      Thread.sleep(1); // Simulate 1ms processing time
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      LOGGER.error("Error during message processing simulation", e);
    }
    long count = messageCounter.incrementAndGet();
    if (count % 100000 == 0) { // Log every 100,000 messages
      LOGGER.info("NormalConsumer: Processed message {} - ID: {}, Timestamp: {}", count, message.getId(), message.getTimestamp());
    }
  }

  public long getMessageCount() {
    return messageCounter.get();
  }

  public void resetCounters() {
    messageCounter.set(0);
  }
}

