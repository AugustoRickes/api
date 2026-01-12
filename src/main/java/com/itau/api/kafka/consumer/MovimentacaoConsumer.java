package com.itau.api.kafka.consumer;

import com.itau.api.dto.TipoMovimentacao;
import com.itau.api.kafka.event.MovimentacaoEvent;
import com.itau.api.mongo.model.MovimentacaoMongo;
import com.itau.api.mongo.repository.MovimentacaoMongoRepository;
import com.itau.api.service.ContratoService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
@Slf4j
public class MovimentacaoConsumer {

    private final ContratoService contratoService;
    private final MovimentacaoMongoRepository movimentacaoMongoRepository; // Inject MongoDB repository

    @KafkaListener(topics = "movimentacoes", groupId = "movimentacoes-group")
    public void consumir(MovimentacaoEvent event) {
        log.info("Consumindo evento de movimentação do Kafka. Evento: {}", event);

        try {
            if (event.getTipo() == TipoMovimentacao.DEBITO) {
                contratoService.registrarDebito(event.getAccountId(), event.getValor());
            } else if (event.getTipo() == TipoMovimentacao.CREDITO) {
                contratoService.registrarCredito(event.getAccountId(), event.getValor());
            }

            MovimentacaoMongo movimentacaoMongo = MovimentacaoMongo.builder()
                    .accountId(event.getAccountId())
                    .valor(event.getValor())
                    .tipo(event.getTipo())
                    .timestamp(LocalDateTime.now())
                    .build();
            movimentacaoMongoRepository.save(movimentacaoMongo);
            log.info("Evento de movimentação para accountId {} salvo no MongoDB.", event.getAccountId());

            log.info("Evento de movimentação processado com sucesso para o accountId: {}", event.getAccountId());
        } catch (Exception e) {
            // todo: melhorar tratamento de erro
            log.error("Erro ao processar evento de movimentação para o accountId: {}. Erro: {}", event.getAccountId(), e.getMessage());
        }
    }
}
