package com.example.kafkabenchmark.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BenchmarkResult {

  private String consumerType;
  private long totalMessages;
  private long timeTakenMillis;
  private double throughputMessagesPerSecond;
  private double averageLatencyPerMessageMillis; // For normal consumer
  private double averageLatencyPerBatchMillis; // For batch consumer
  private long messagesPerBatchAvg; // For batch consumer
  private long totalBatches; // For batch consumer
  private long initialMemoryUsedBytes;
  private long finalMemoryUsedBytes;
  private long maxMemoryBytes;
  private double averageCpuLoad; // System CPU Load if available
  private String gcActivity; // Simple representation
  private int peakThreadCount;
  private int finalThreadCount;
}

