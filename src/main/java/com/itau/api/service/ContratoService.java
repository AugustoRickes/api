package com.itau.api.service;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.itau.api.dto.ContratoRequestDTO;
import com.itau.api.dto.ContratoResponseDTO;
import com.itau.api.exception.ResourceNotFoundException;
import com.itau.api.model.Contrato;
import com.itau.api.repository.ContratoRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ContratoService {

    private final ContratoRepository contratoRepository;

    public ContratoResponseDTO criarContrato(ContratoRequestDTO request) {
        Optional<Contrato> existingContrato = contratoRepository.findByAccountId(request.getAccountId());
        if (existingContrato.isPresent()) {
            throw new IllegalArgumentException("Contrato já existente para este accountId.");
        }

        Contrato contrato = Contrato.builder()
                .accountId(request.getAccountId())
                .valorLimite(request.getValorLimite())
                .saldoDevedor(BigDecimal.ZERO)
                .build();

        Contrato savedContrato = contratoRepository.save(contrato);
        return toResponseDTO(savedContrato);
    }

    public ContratoResponseDTO consultarContrato(String accountId) {
        Contrato contrato = findContratoByAccountId(accountId);
        return toResponseDTO(contrato);
    }

    public ContratoResponseDTO alterarLimite(String accountId, BigDecimal novoValorLimite) {
        Contrato contrato = findContratoByAccountId(accountId);

        if (novoValorLimite.compareTo(contrato.getSaldoDevedor()) < 0) {
            throw new IllegalStateException("O valor do limite não pode ser inferior ao saldo devedor atual.");
        }

        contrato.setValorLimite(novoValorLimite);
        
        Contrato savedContrato = contratoRepository.save(contrato);
        return toResponseDTO(savedContrato);
    }

    public void cancelarContrato(String accountId) {
        Contrato contrato = findContratoByAccountId(accountId);
        if (contrato.getSaldoDevedor().compareTo(BigDecimal.ZERO) > 0) {
            throw new IllegalStateException("Não é possível cancelar um contrato com saldo devedor positivo.");
        }
        contratoRepository.delete(contrato);
    }

    public ContratoResponseDTO registrarDebito(String accountId, BigDecimal valor) {
        Contrato contrato = findContratoByAccountId(accountId);
        BigDecimal limiteDisponivel = contrato.getValorLimite().subtract(contrato.getSaldoDevedor());
        
        if (limiteDisponivel.compareTo(valor) < 0) {
            throw new IllegalStateException("Débito não permitido. Limite disponível insuficiente.");
        }
        
        contrato.setSaldoDevedor(contrato.getSaldoDevedor().add(valor));
        Contrato savedContrato = contratoRepository.save(contrato);
        return toResponseDTO(savedContrato);
    }

    public ContratoResponseDTO registrarCredito(String accountId, BigDecimal valor) {
        Contrato contrato = findContratoByAccountId(accountId);
        BigDecimal novoSaldoDevedor = contrato.getSaldoDevedor().subtract(valor);

        if (novoSaldoDevedor.compareTo(BigDecimal.ZERO) < 0) {
            novoSaldoDevedor = BigDecimal.ZERO;
        }

        contrato.setSaldoDevedor(novoSaldoDevedor);
        Contrato savedContrato = contratoRepository.save(contrato);
        return toResponseDTO(savedContrato);
    }
    
    private Contrato findContratoByAccountId(String accountId) {
        return contratoRepository.findByAccountId(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato não encontrado para o accountId: " + accountId));
    }

    private ContratoResponseDTO toResponseDTO(Contrato contrato) {
        BigDecimal limiteDisponivel = contrato.getValorLimite().subtract(contrato.getSaldoDevedor());
        if (limiteDisponivel.compareTo(BigDecimal.ZERO) < 0) {
            limiteDisponivel = BigDecimal.ZERO;
        }
        
        return ContratoResponseDTO.builder()
                .accountId(contrato.getAccountId())
                .valorLimite(contrato.getValorLimite())
                .saldoDevedor(contrato.getSaldoDevedor())
                .limiteDisponivel(limiteDisponivel)
                .build();
    }
}
