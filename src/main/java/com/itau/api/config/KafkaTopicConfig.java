package com.itau.api.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    public static final String TOPIC_MOVIMENTACOES = "movimentacoes";

    @Bean
    public NewTopic movimentacoesTopic() {
        return TopicBuilder.name(TOPIC_MOVIMENTACOES)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
