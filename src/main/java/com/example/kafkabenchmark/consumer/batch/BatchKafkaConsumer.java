package com.example.kafkabenchmark.consumer.batch;

import com.example.kafkabenchmark.model.BenchmarkMessage;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class BatchKafkaConsumer {

  private static final Logger LOGGER = LoggerFactory.getLogger(BatchKafkaConsumer.class);
  private final AtomicLong messageCounter = new AtomicLong(0);
  private final AtomicLong batchCounter = new AtomicLong(0);

  @KafkaListener(topics = "${kafka.topic.batch}", groupId = "batch-consumer-group", containerFactory = "batchKafkaListenerContainerFactory")
  public void receiveBatchMessages(List<BenchmarkMessage> messages) {
    // Simulate some processing for the batch
    try {
      // Simulate a small base processing time for the batch itself, plus per-message processing
      Thread.sleep(1 + (long) messages.size() / 10); // e.g. 1ms base + 0.1ms per message in batch
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      LOGGER.error("Error during batch message processing simulation", e);
    }
    long currentBatchNumber = batchCounter.incrementAndGet();
    long messagesInBatch = messages.size();
    long totalMessagesProcessed = messageCounter.addAndGet(messagesInBatch);

    if (currentBatchNumber % 1000 == 0) { // Log every 1000 batches
      LOGGER.info("BatchConsumer: Processed batch #{} with {} messages. Total messages processed: {}. Last message ID in batch: {}",
          currentBatchNumber, messagesInBatch, totalMessagesProcessed, messages.get(messages.size() - 1).getId());
    }
  }

  public long getMessageCount() {
    return messageCounter.get();
  }

  public long getBatchCount() {
    return batchCounter.get();
  }

  public void resetCounters() {
    messageCounter.set(0);
    batchCounter.set(0);
  }
}

