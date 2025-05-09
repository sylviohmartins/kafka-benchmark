package com.example.kafkabenchmark.util;

import com.example.kafkabenchmark.model.BenchmarkMessage;
import java.util.UUID;

public class BenchmarkTestUtils {

  public static final int TOTAL_MESSAGES = 50_000;
  public static final int MESSAGE_PAYLOAD_SIZE_KB = 1;
  public static final String NORMAL_TOPIC_NAME = "normal.topic";
  public static final String BATCH_TOPIC_NAME = "batch.topic";

  public static BenchmarkMessage createBenchmarkMessage(long sequence) {
    String id = UUID.randomUUID().toString() + "-" + sequence;
    // Create a payload of approximately 1KB
    // A char is 2 bytes in Java. So 500 chars will be 1000 bytes = 1KB.
    // Add some overhead for JSON structure (id, payload, timestamp fields).
    // Let's aim for a payload string that results in roughly 1KB JSON.
    // Payload itself around 900 chars to be safe with JSON overhead.
    StringBuilder payloadBuilder = new StringBuilder(900);
    for (int i = 0; i < 90; i++) { // 90 * 10 chars = 900 chars
      payloadBuilder.append("0123456789");
    }
    return new BenchmarkMessage(id, payloadBuilder.toString(), System.currentTimeMillis());
  }
}

