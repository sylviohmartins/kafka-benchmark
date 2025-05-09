package com.example.kafkabenchmark.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BenchmarkMessage {

  private String id;
  private String payload;
  private long timestamp;
}

