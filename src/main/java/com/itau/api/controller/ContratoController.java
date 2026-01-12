package com.itau.api.controller;

import com.itau.api.dto.*;
import com.itau.api.kafka.event.MovimentacaoEvent;
import com.itau.api.kafka.producer.MovimentacaoProducer;
import com.itau.api.service.ContratoService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/contratos")
@AllArgsConstructor
public class ContratoController {

    private final ContratoService contratoService;
    private final MovimentacaoProducer movimentacaoProducer;

    /**
     * Endpoint para criar um novo contrato de limite.
     * @param request Corpo da requisição com dados para criação do contrato.
     * @return ResponseEntity com status 201 (Created) e os dados do contrato criado.
     */
    @PostMapping
    public ResponseEntity<ContratoResponseDTO> criarContrato(@Valid @RequestBody ContratoRequestDTO request) {
        ContratoResponseDTO response = contratoService.criarContrato(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Endpoint para consultar um contrato de limite pelo ID da conta.
     * @param accountId O ID da conta a ser consultada.
     * @return ResponseEntity com status 200 (OK) e os dados do contrato.
     */
    @GetMapping("/{accountId}")
    public ResponseEntity<ContratoResponseDTO> consultarContrato(@PathVariable String accountId) {
        ContratoResponseDTO response = contratoService.consultarContrato(accountId);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para alterar o valor do limite de um contrato.
     * @param accountId O ID da conta do contrato a ser alterado.
     * @param request Corpo da requisição com o novo valor de limite.
     * @return ResponseEntity com status 200 (OK) and os dados do contrato atualizado.
     */
    @PutMapping("/{accountId}/limite")
    public ResponseEntity<ContratoResponseDTO> alterarLimite(
            @PathVariable String accountId,
            @RequestBody AlterarLimiteRequestDTO request) {
        ContratoResponseDTO response = contratoService.alterarLimite(accountId, request.getValor());
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para cancelar um contrato de limite.
     * @param accountId O ID da conta do contrato a ser cancelado.
     */
    @DeleteMapping("/{accountId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelarContrato(@PathVariable String accountId) {
        contratoService.cancelarContrato(accountId);
    }

    /**
     * Aceita uma solicitação de débito e a envia para processamento assíncrono.
     * @param accountId O ID da conta.
     * @param request Corpo da requisição com o valor a ser debitado.
     * @return ResponseEntity com status 202 (Accepted).
     */
    @PostMapping("/{accountId}/debito")
    public ResponseEntity<Void> registrarDebito(
            @PathVariable String accountId,
            @RequestBody MovimentacaoRequestDTO request) {

        MovimentacaoEvent event = new MovimentacaoEvent(accountId, request.getValor(), TipoMovimentacao.DEBITO);
        movimentacaoProducer.enviar(event);

        return ResponseEntity.accepted().build();
    }

    /**
     * Aceita uma solicitação de crédito e a envia para processamento assíncrono.
     * @param accountId O ID da conta.
     * @param request Corpo da requisição com o valor a ser creditado.
     * @return ResponseEntity com status 202 (Accepted).
     */
    @PostMapping("/{accountId}/credito")
    public ResponseEntity<Void> registrarCredito(
            @PathVariable String accountId,
            @RequestBody MovimentacaoRequestDTO request) {
        
        MovimentacaoEvent event = new MovimentacaoEvent(accountId, request.getValor(), TipoMovimentacao.CREDITO);
        movimentacaoProducer.enviar(event);

        return ResponseEntity.accepted().build();
    }
}

