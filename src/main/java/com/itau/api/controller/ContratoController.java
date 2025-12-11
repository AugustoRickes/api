package com.itau.api.controller;

import java.math.BigDecimal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.itau.api.dto.AlterarLimiteRequestDTO;
import com.itau.api.dto.ContratoRequestDTO;
import com.itau.api.dto.ContratoResponseDTO;
import com.itau.api.dto.MovimentacaoRequestDTO;
import com.itau.api.service.ContratoService;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/contratos")
@AllArgsConstructor
public class ContratoController {

    private final ContratoService contratoService;

    @PostMapping
    public ResponseEntity<ContratoResponseDTO> criarContrato(@RequestBody ContratoRequestDTO request) {
        ContratoResponseDTO response = contratoService.criarContrato(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<ContratoResponseDTO> consultarContrato(@PathVariable String accountId) {
        ContratoResponseDTO response = contratoService.consultarContrato(accountId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{accountId}/limite")
    public ResponseEntity<ContratoResponseDTO> alterarLimite(
            @PathVariable String accountId,
            @RequestBody AlterarLimiteRequestDTO request) {
        ContratoResponseDTO response = contratoService.alterarLimite(accountId, request.getValor());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{accountId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelarContrato(@PathVariable String accountId) {
        contratoService.cancelarContrato(accountId);
    }

    @PostMapping("/{accountId}/debito")
    public ResponseEntity<ContratoResponseDTO> registrarDebito(
            @PathVariable String accountId,
            @RequestBody MovimentacaoRequestDTO request) {
        ContratoResponseDTO response = contratoService.registrarDebito(accountId, request.getValor());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{accountId}/credito")
    public ResponseEntity<ContratoResponseDTO> registrarCredito(
            @PathVariable String accountId,
            @RequestBody MovimentacaoRequestDTO request) {
        ContratoResponseDTO response = contratoService.registrarCredito(accountId, request.getValor());
        return ResponseEntity.ok(response);
    }
}
