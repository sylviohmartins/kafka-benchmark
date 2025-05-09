## Análise dos Resultados do Benchmark Kafka (50.000 Mensagens)

Esta seção consolida e analisa os resultados dos testes de benchmark executados com um volume de 50.000 mensagens para os consumidores Kafka Normal e em Lote, utilizando Spring Boot 3.4.5 e Java 21.

### Resultados Coletados dos Logs (50.000 mensagens):

**Consumidor Normal (50.000 mensagens):**

```
2025-05-08T00:03:11.999-04:00  INFO 11186 --- [           main] c.e.k.KafkaBenchmarkApplicationTests     : Benchmark result for Normal: BenchmarkResult(consumerType=Normal, totalMessages=50000, timeTakenMillis=57632, throughputMessagesPerSecond=867.5735700305386, averageLatencyPerMessageMillis=1.0, averageLatencyPerBatchMillis=0.0, messagesPerBatchAvg=0, totalBatches=0, initialMemoryUsedBytes=111890440, finalMemoryUsedBytes=147913240, maxMemoryBytes=4294967296, averageCpuLoad=0.0, gcActivity=GC Collections: 24, GC Time: 243 ms, peakThreadCount=43, finalThreadCount=43)
```

**Consumidor em Lote (50.000 mensagens):**

```
2025-05-08T00:03:44.257-04:00  INFO 11715 --- [           main] c.e.k.KafkaBenchmarkApplicationTests     : Benchmark result for Batch: BenchmarkResult(consumerType=Batch, totalMessages=50000, timeTakenMillis=8087, throughputMessagesPerSecond=6181.525905774699, averageLatencyPerMessageMillis=0.0, averageLatencyPerBatchMillis=77.01904761904762, messagesPerBatchAvg=476, totalBatches=105, initialMemoryUsedBytes=112433760, finalMemoryUsedBytes=127993096, maxMemoryBytes=4294967296, averageCpuLoad=0.0, gcActivity=GC Collections: 7, GC Time: 71 ms, peakThreadCount=43, finalThreadCount=43)
```

### Consolidação dos Resultados (50.000 Mensagens):

| Métrica                          | Consumidor Normal | Consumidor em Lote | Unidade         |
|----------------------------------|-------------------|--------------------|-----------------|
| Volume de Mensagens              | 50.000            | 50.000             | mensagens       |
| Tempo Total Gasto                | 57.632            | 8.087              | segundos        |
| Vazão (Throughput)               | 867.57            | 6181.53            | msg/seg         |
| Latência Média por Mensagem      | 1.0 (simulada)    | N/A                | ms              |
| Total de Lotes Processados       | N/A               | 105                | lotes           |
| Média de Mensagens por Lote      | N/A               | 476                | msg/lote        |
| Latência Média por Lote          | N/A               | 77.02              | ms/lote         |
| Uso Inicial de Memória (Heap)    | 111.89            | 112.43             | MB              |
| Uso Final de Memória (Heap)      | 147.91            | 127.99             | MB              |
| Variação de Memória (Heap)       | +36.02            | +15.56             | MB              |
| Coleções de GC                   | 24                | 7                  | contagem        |
| Tempo de GC                      | 243               | 71                 | ms              |
| Pico de Threads                  | 43                | 43                 | contagem        |
| Carga Média de CPU (Sistema)     | 0.0               | 0.0                | (0.0-1.0)       |

**Análise Preliminar (50.000 mensagens):**

1.  **Vazão (Throughput):** O consumidor em lote demonstrou uma vazão drasticamente superior (aproximadamente **7.1 vezes mais rápido**) em comparação com o consumidor normal.
2.  **Tempo de Processamento:** O consumidor em lote foi significativamente mais rápido, completando a tarefa em cerca de 14% do tempo levado pelo consumidor normal.
3.  **Uso de Memória:** O consumidor normal teve um aumento maior no uso de memória heap (+36.02 MB) em comparação com o consumidor em lote (+15.56 MB). O consumo inicial foi similar.
4.  **Atividade do Garbage Collector (GC):** O consumidor em lote teve consideravelmente menos coleções de GC (7 vs. 24) e um tempo total de GC muito menor (71 ms vs. 243 ms), indicando maior eficiência.
5.  **Latência:** A latência por mensagem no consumidor normal foi de 1ms (simulada). No consumidor em lote, a latência média por lote foi de 77.02 ms, com uma média de 476 mensagens por lote. A simulação de processamento para um lote de 476 mensagens seria `1 + 476/10 ≈ 48.6ms`. O tempo restante (77.02 - 48.6 ≈ 28.42ms) por lote pode ser atribuído ao polling, desserialização e outras operações.
6.  **CPU e Threads:** A carga de CPU do sistema foi baixa para ambos, e o pico de threads foi idêntico.

**Conclusão Preliminar (50.000 mensagens):**

Para um volume de 50.000 mensagens, o consumidor em lote continua a ser marcadamente mais eficiente em termos de vazão e uso de recursos (memória e GC). A diferença de desempenho é ainda mais pronunciada do que no teste com 10.000 mensagens.

Próximos passos: Estruturar o relatório final em Markdown para esta rodada de testes.
