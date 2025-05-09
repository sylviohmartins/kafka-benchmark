package com.example.kafkabenchmark;

import com.example.kafkabenchmark.consumer.batch.BatchKafkaConsumer;
import com.example.kafkabenchmark.consumer.normal.NormalKafkaConsumer;
import com.example.kafkabenchmark.model.BenchmarkMessage;
import com.example.kafkabenchmark.model.BenchmarkResult;
import com.example.kafkabenchmark.util.BenchmarkTestUtils;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest
// Using EmbeddedKafka for simplicity and direct control over topics for this benchmark
@EmbeddedKafka(partitions = 1, topics = {BenchmarkTestUtils.NORMAL_TOPIC_NAME, BenchmarkTestUtils.BATCH_TOPIC_NAME})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS) // Ensure Kafka is reset for other test classes if any
public class KafkaBenchmarkApplicationTests {

  private static final Logger LOGGER = LoggerFactory.getLogger(KafkaBenchmarkApplicationTests.class);

  @Autowired
  private KafkaTemplate<String, BenchmarkMessage> kafkaTemplate;

  @Autowired
  private NormalKafkaConsumer normalKafkaConsumer;

  @Autowired
  private BatchKafkaConsumer batchKafkaConsumer;

  private OperatingSystemMXBean osMxBean;
  private MemoryMXBean memoryMxBean;
  private ThreadMXBean threadMxBean;
  private List<GarbageCollectorMXBean> gcMxBeans;

  private static BenchmarkResult normalConsumerResult;
  private static BenchmarkResult batchConsumerResult;

  @BeforeEach
  void setUp() {
    osMxBean = ManagementFactory.getOperatingSystemMXBean();
    memoryMxBean = ManagementFactory.getMemoryMXBean();
    threadMxBean = ManagementFactory.getThreadMXBean();
    gcMxBeans = ManagementFactory.getGarbageCollectorMXBeans();

    normalKafkaConsumer.resetCounters();
    batchKafkaConsumer.resetCounters();
    // Ensure topics are created - EmbeddedKafka handles this based on annotation
  }

  @AfterEach
  void tearDown() {
    // Any cleanup if necessary
  }

  private void produceMessages(String topic, int numMessages) throws InterruptedException {
    LOGGER.info("Starting to produce {} messages to topic: {}", numMessages, topic);
    for (int i = 0; i < numMessages; i++) {
      BenchmarkMessage message = BenchmarkTestUtils.createBenchmarkMessage(i);
      kafkaTemplate.send(topic, message.getId(), message);
      if ((i + 1) % 100000 == 0) {
        LOGGER.info("Produced {} messages to topic: {}", i + 1, topic);
        // Brief pause to allow consumer to catch up slightly and avoid overwhelming producer buffer too quickly
        // This is not ideal for a pure throughput test of producer, but helps stabilize consumer tests
        // Thread.sleep(50);
      }
    }
    kafkaTemplate.flush();
    LOGGER.info("Finished producing {} messages to topic: {}", numMessages, topic);
  }

  private BenchmarkResult runBenchmark(String consumerType, String topic, Runnable produceTrigger, Runnable resetConsumer, LongSupplier getTotalMessagesConsumed, LongSupplier getTotalBatchesProcessed) throws InterruptedException {
    LOGGER.info("Starting benchmark for {} consumer on topic {}", consumerType, topic);
    resetConsumer.run();

    long initialMemory = memoryMxBean.getHeapMemoryUsage().getUsed();
    long initialGcCount = gcMxBeans.stream().mapToLong(GarbageCollectorMXBean::getCollectionCount).sum();
    long initialGcTime = gcMxBeans.stream().mapToLong(GarbageCollectorMXBean::getCollectionTime).sum();
    int initialThreadCount = threadMxBean.getThreadCount();
    threadMxBean.resetPeakThreadCount(); // Reset peak for this test run

    long startTime = System.currentTimeMillis();

    produceTrigger.run();

    // Wait for all messages to be consumed
    CountDownLatch latch = new CountDownLatch(1);
    long expectedMessages = BenchmarkTestUtils.TOTAL_MESSAGES;
    long pollIntervalMs = 2000; // Check every 2 seconds
    long timeoutMs = 25 * 60 * 1000; // 25 minutes timeout for consuming all messages
    long startTimeConsumerWait = System.currentTimeMillis();

    while (getTotalMessagesConsumed.getAsLong() < expectedMessages) {
      if (System.currentTimeMillis() - startTimeConsumerWait > timeoutMs) {
        LOGGER.error("Timeout waiting for {} consumer to process all messages. Processed: {}/{}", consumerType, getTotalMessagesConsumed.getAsLong(), expectedMessages);
        throw new RuntimeException("Timeout waiting for consumer");
      }
      LOGGER.info("{} consumer: Processed {}/{} messages. Waiting...", consumerType, getTotalMessagesConsumed.getAsLong(), expectedMessages);
      Thread.sleep(pollIntervalMs);
    }
    latch.countDown();
    latch.await(1, TimeUnit.MILLISECONDS); // Ensure previous log is flushed

    long endTime = System.currentTimeMillis();
    long timeTaken = endTime - startTime;

    long finalMemory = memoryMxBean.getHeapMemoryUsage().getUsed();
    long finalGcCount = gcMxBeans.stream().mapToLong(GarbageCollectorMXBean::getCollectionCount).sum();
    long finalGcTime = gcMxBeans.stream().mapToLong(GarbageCollectorMXBean::getCollectionTime).sum();
    int peakThreadCount = threadMxBean.getPeakThreadCount();
    int finalThreadCount = threadMxBean.getThreadCount();
    double cpuLoad = osMxBean.getSystemLoadAverage(); // This is system load, might not be process specific without more tools

    long totalMessages = getTotalMessagesConsumed.getAsLong();
    double throughput = (double) totalMessages * 1000 / timeTaken;
    String gcActivitySummary = String.format("GC Collections: %d, GC Time: %d ms", (finalGcCount - initialGcCount), (finalGcTime - initialGcTime));

    BenchmarkResult.BenchmarkResultBuilder builder = BenchmarkResult.builder()
        .consumerType(consumerType)
        .totalMessages(totalMessages)
        .timeTakenMillis(timeTaken)
        .throughputMessagesPerSecond(throughput)
        .initialMemoryUsedBytes(initialMemory)
        .finalMemoryUsedBytes(finalMemory)
        .maxMemoryBytes(memoryMxBean.getHeapMemoryUsage().getMax())
        .averageCpuLoad(cpuLoad) // Note: System CPU Load
        .gcActivity(gcActivitySummary)
        .peakThreadCount(peakThreadCount)
        .finalThreadCount(finalThreadCount);

    if (consumerType.equalsIgnoreCase("Normal")) {
      // Latency for normal consumer is tricky to measure accurately here without message-level timestamps and processing hooks.
      // The current Thread.sleep(1) in consumer dominates this. So, effective latency is roughly that sleep time.
      builder.averageLatencyPerMessageMillis(1.0); // Placeholder, as actual processing is Thread.sleep(1)
    }
    if (consumerType.equalsIgnoreCase("Batch")) {
      long totalBatches = getTotalBatchesProcessed.getAsLong();
      builder.totalBatches(totalBatches);
      if (totalBatches > 0) {
        builder.messagesPerBatchAvg(totalMessages / totalBatches);
        // Similar latency note as normal consumer. The Thread.sleep in batch consumer is (1 + size/10).
        // This is an approximation.
        builder.averageLatencyPerBatchMillis((double) timeTaken / totalBatches);
      }
    }

    BenchmarkResult result = builder.build();
    LOGGER.info("Benchmark result for {}: {}", consumerType, result);
    return result;
  }

  @Test
  public void benchmarkNormalConsumer() throws InterruptedException {
    LOGGER.info("--- Starting Benchmark for Normal Consumer ---");
    normalConsumerResult = runBenchmark("Normal",
        BenchmarkTestUtils.NORMAL_TOPIC_NAME,
        () -> {
          try {
            produceMessages(BenchmarkTestUtils.NORMAL_TOPIC_NAME, BenchmarkTestUtils.TOTAL_MESSAGES);
          } catch (InterruptedException e) {
            throw new RuntimeException(e);
          }
        },
        () -> normalKafkaConsumer.resetCounters(),
        () -> normalKafkaConsumer.getMessageCount(),
        () -> 0 // No batches for normal consumer
    );
    LOGGER.info("--- Finished Benchmark for Normal Consumer ---");
  }

  @Test
  public void benchmarkBatchConsumer() throws InterruptedException {
    LOGGER.info("--- Starting Benchmark for Batch Consumer ---");
    // Ensure normal consumer test has finished or run separately to avoid interference if topics/consumers are not perfectly isolated.
    // @DirtiesContext helps, but running tests sequentially is safer for benchmarks.
    // For JUnit 5, use @Order if needed, or separate test classes.

    batchConsumerResult = runBenchmark("Batch",
        BenchmarkTestUtils.BATCH_TOPIC_NAME,
        () -> {
          try {
            produceMessages(BenchmarkTestUtils.BATCH_TOPIC_NAME, BenchmarkTestUtils.TOTAL_MESSAGES);
          } catch (InterruptedException e) {
            throw new RuntimeException(e);
          }
        },
        () -> batchKafkaConsumer.resetCounters(),
        () -> batchKafkaConsumer.getMessageCount(),
        () -> batchKafkaConsumer.getBatchCount()
    );
    LOGGER.info("--- Finished Benchmark for Batch Consumer ---");
  }

  // A final test to print results, could be part of a @AfterAll method if needed
  // Or results are logged, and we'll collect them for the report later.
  // For now, static variables hold the results, which is not ideal for parallel tests but fine for sequential ones.
  // The report generation step will use these.
}



