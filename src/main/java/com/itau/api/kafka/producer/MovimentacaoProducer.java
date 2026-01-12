package com.itau.api.kafka.producer;

import com.itau.api.config.KafkaTopicConfig;
import com.itau.api.kafka.event.MovimentacaoEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class MovimentacaoProducer {

    private final KafkaTemplate<String, MovimentacaoEvent> kafkaTemplate;

    public void enviar(MovimentacaoEvent event) {
        log.info("Enviando evento de movimentação para o Kafka. Evento: {}", event);
        kafkaTemplate.send(KafkaTopicConfig.TOPIC_MOVIMENTACOES, event.getAccountId(), event);
    }
}

