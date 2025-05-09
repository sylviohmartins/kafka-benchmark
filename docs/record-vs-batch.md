# Relatório Final do Benchmark: Consumidor Kafka Normal vs. em Lote (50.000 Mensagens)

## 1. Introdução

Este relatório apresenta os resultados de uma segunda rodada de benchmarks, realizada para comparar o desempenho de um consumidor Kafka configurado para processar mensagens individualmente (Normal) versus um consumidor configurado para processar mensagens em lote (Batch), desta vez com um volume de **50.000 mensagens**. O objetivo permanece analisar o uso geral de infraestrutura (consumo de CPU, memória) e velocidade (vazão, latência).

Os testes foram conduzidos utilizando **Spring Boot 3.4.5**, Java 21 e Kafka Embedded (via Testcontainers) para simular um ambiente de produção de forma controlada, atendendo à solicitação de atualização da stack e volume de mensagens.

## 2. Metodologia

### 2.1. Parâmetros do Benchmark

Os seguintes parâmetros foram definidos para esta rodada de testes:

*   **Volume de Mensagens:** 50.000 mensagens para ambos os consumidores.
*   **Tamanho das Mensagens:** Aproximadamente 1KB por mensagem.
*   **Taxa de Produção de Mensagens:** O produtor enviou mensagens o mais rápido possível.
*   **Configurações do Consumidor em Lote (Batch):**
    *   Tamanho máximo do lote (`spring.kafka.consumer.max-poll-records`): 500 mensagens.
    *   Tempo limite para formação do lote (`spring.kafka.consumer.fetch-max-wait`): 100 milissegundos.
*   **Simulação de Processamento:**
    *   Consumidor Normal: `Thread.sleep(1)` por mensagem.
    *   Consumidor em Lote: `Thread.sleep(1 + (long)messages.size() / 10)` por lote.
*   **Métricas Principais Coletadas:** Vazão, latência, consumo de CPU, uso de memória JVM, atividade do GC, número de threads.
*   **Duração dos Testes:** Até que todas as 50.000 mensagens fossem consumidas, com um timeout de 25 minutos (que não foi atingido para este volume).

### 2.2. Ambiente de Teste

*   **Plataforma:** Sandbox Linux (Ubuntu 22.04 LTS amd64).
*   **Java:** OpenJDK 21.
*   **Spring Boot:** 3.4.5 (conforme `pom.xml`).
*   **Spring Kafka:** Versão gerenciada pelo Spring Boot 3.4.5.
*   **Apache Kafka:** Kafka Embedded fornecido pela dependência `spring-kafka-test` e `org.testcontainers:kafka`.
*   **Build Tool:** Apache Maven (via `mvnw`).

## 3. Configuração do Projeto

A configuração do projeto permaneceu a mesma da rodada anterior, com a exceção da versão do Spring Boot atualizada para 3.4.5 no arquivo `pom.xml` e o volume de mensagens ajustado para 50.000 na classe `BenchmarkTestUtils.java`.

## 4. Resultados Detalhados (50.000 mensagens)

Os resultados foram coletados a partir dos logs de execução dos testes Maven.

*   **Log de Resultado (Consumidor Normal - 50.000 mensagens):**
```
2025-05-08T00:03:11.999-04:00  INFO 11186 --- [           main] c.e.k.KafkaBenchmarkApplicationTests     : Benchmark result for Normal: BenchmarkResult(consumerType=Normal, totalMessages=50000, timeTakenMillis=57632, throughputMessagesPerSecond=867.5735700305386, averageLatencyPerMessageMillis=1.0, averageLatencyPerBatchMillis=0.0, messagesPerBatchAvg=0, totalBatches=0, initialMemoryUsedBytes=111890440, finalMemoryUsedBytes=147913240, maxMemoryBytes=4294967296, averageCpuLoad=0.0, gcActivity=GC Collections: 24, GC Time: 243 ms, peakThreadCount=43, finalThreadCount=43)
```

*   **Log de Resultado (Consumidor em Lote - 50.000 mensagens):**
```
2025-05-08T00:03:44.257-04:00  INFO 11715 --- [           main] c.e.k.KafkaBenchmarkApplicationTests     : Benchmark result for Batch: BenchmarkResult(consumerType=Batch, totalMessages=50000, timeTakenMillis=8087, throughputMessagesPerSecond=6181.525905774699, averageLatencyPerMessageMillis=0.0, averageLatencyPerBatchMillis=77.01904761904762, messagesPerBatchAvg=476, totalBatches=105, initialMemoryUsedBytes=112433760, finalMemoryUsedBytes=127993096, maxMemoryBytes=4294967296, averageCpuLoad=0.0, gcActivity=GC Collections: 7, GC Time: 71 ms, peakThreadCount=43, finalThreadCount=43)
```

*   **Tabela Consolidada de Resultados (50.000 mensagens):**

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


## 5. Análise Comparativa (50.000 mensagens)

Analisando os resultados do benchmark com 50.000 mensagens, observamos o seguinte:

*   **Vazão (Throughput):** O consumidor em lote demonstrou uma performance drasticamente superior, atingindo uma vazão de **6181.53 msg/seg**, enquanto o consumidor normal alcançou **867.57 msg/seg**. Isso representa um ganho de aproximadamente **7.1 vezes** em favor do consumidor em lote, um aumento na diferença de performance em relação ao teste com 10.000 mensagens.

*   **Tempo de Processamento:** Como reflexo direto da maior vazão, o consumidor em lote completou o processamento das 50.000 mensagens em **8.087 segundos**, significativamente mais rápido que os **57.632 segundos** do consumidor normal.

*   **Uso de Memória (Heap):** Ambos os consumidores iniciaram com um uso de heap similar (aproximadamente 112 MB). O consumidor normal apresentou um incremento maior no uso de memória ao final do teste (+36.02 MB) em comparação com o consumidor em lote (+15.56 MB). Isso reforça a observação de que o processamento em lote pode ser mais eficiente na gestão de memória.

*   **Atividade do Garbage Collector (GC):** O consumidor em lote resultou em menos coleções de GC (7 vs. 24) e um tempo total de GC consideravelmente menor (71 ms vs. 243 ms). Menor atividade de GC contribui para uma performance mais estável.

*   **Latência:**
    *   Para o **consumidor normal**, a latência de processamento por mensagem foi dominada pela simulação de `Thread.sleep(1)`.
    *   Para o **consumidor em lote**, a média de mensagens por lote foi de 476. A latência média por lote foi de 77.02 ms. A simulação de processamento para um lote de 476 mensagens seria `1 + 476/10 ≈ 48.6ms`. O tempo restante (77.02 - 48.6 ≈ 28.42ms) por lote pode ser atribuído ao polling do Kafka, desserialização e outras operações.

*   **CPU e Threads:** A carga média de CPU do sistema foi muito baixa para ambos os cenários (0.0), e o pico de threads foi idêntico (43 threads).

## 6. Discussão e Observações Adicionais

*   **Escalabilidade da Vantagem do Lote:** A vantagem do consumidor em lote em termos de vazão tornou-se ainda mais pronunciada com o aumento do volume de mensagens de 10.000 para 50.000. Isso sugere que, para volumes maiores, os benefícios do processamento em lote tendem a se acentuar.
*   **Impacto da Simulação de Processamento:** A simulação de `Thread.sleep()` continua sendo um fator determinante na latência observada. Em cenários reais, a complexidade do processamento influenciará diretamente esses números.
*   **Spring Boot 3.4.5 e Java 21:** A execução com a stack atualizada não apresentou problemas de compatibilidade ou regressão de performance visíveis em comparação com a estrutura do teste anterior, embora uma comparação direta de performance entre versões do Spring Boot não fosse o foco principal deste ajuste.

## 7. Conclusões (para 50.000 mensagens)

Com base nos resultados do benchmark para 50.000 mensagens:

1.  **O consumidor em lote (Batch) é substancialmente mais performático que o consumidor normal**, com uma diferença de vazão ainda maior do que a observada com 10.000 mensagens.
2.  O consumidor em lote continua a demonstrar **maior eficiência no uso de recursos**, especialmente em termos de atividade do GC e incremento de memória heap.
3.  Para cenários que processam volumes moderados a altos de mensagens e onde a otimização de vazão e recursos é crucial, **a abordagem de consumidor em lote é a escolha preferencial**.

## 8. Recomendações

As recomendações da rodada anterior de testes permanecem válidas e são reforçadas pelos resultados com 50.000 mensagens:

*   **Priorizar Consumidores em Lote:** Para a maioria dos casos de uso que visam alta taxa de transferência e uso eficiente de recursos, o consumidor em lote é recomendado.
*   **Ajuste Fino de Parâmetros:** Continuar a ajustar `max.poll.records` e `fetch.max.wait.ms` (ou `spring.kafka.listener.batch.timeout`) é essencial para otimizar o desempenho do consumidor em lote para a carga de trabalho específica.
*   **Monitoramento em Produção:** É fundamental monitorar as métricas de desempenho em um ambiente de produção para ajustes contínuos.
*   **Testes com Carga Realista:** Sempre que possível, realizar testes com dados e lógica de processamento que espelhem o ambiente de produção.

## 9. Referências

*   Documentação do Spring for Apache Kafka: [https://docs.spring.io/spring-kafka/reference/html/](https://docs.spring.io/spring-kafka/reference/html/)
*   Documentação do Apache Kafka: [https://kafka.apache.org/documentation/](https://kafka.apache.org/documentation/)
*   Spring Boot Documentation (incluindo 3.4.5): [https://docs.spring.io/spring-boot/index.html](https://docs.spring.io/spring-boot/index.html)
*   Testcontainers Documentation: [https://www.testcontainers.org/](https://www.testcontainers.org/)

## 10. Código Fonte do Projeto

O código fonte completo do projeto de benchmark está localizado no diretório `/home/ubuntu/kafka-benchmark/`.

--- Fim do Relatório (50.000 mensagens) ---
