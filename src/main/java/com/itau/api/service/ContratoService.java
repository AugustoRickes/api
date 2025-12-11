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

    /**
     * Cria um novo contrato de limite para uma conta.
     * O saldo devedor é inicializado com zero.
     *
     * @param request O DTO contendo o accountId e o valor do limite a ser contratado.
     * @return O DTO de resposta com os dados do contrato criado.
     * @throws IllegalArgumentException se um contrato já existir para o accountId informado.
     */
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

    /**
     * Consulta os dados de um contrato de limite a partir do accountId.
     *
     * @param accountId O identificador da conta.
     * @return O DTO de resposta com os dados do contrato, incluindo o limite disponível.
     * @throws ResourceNotFoundException se nenhum contrato for encontrado para o accountId informado.
     */
    public ContratoResponseDTO consultarContrato(String accountId) {
        Contrato contrato = findContratoByAccountId(accountId);
        return toResponseDTO(contrato);
    }

    /**
     * Altera o valor do limite total de um contrato existente.
     *
     * @param accountId O identificador da conta.
     * @param novoValorLimite O novo valor total do limite.
     * @return O DTO de resposta com os dados do contrato atualizado.
     * @throws ResourceNotFoundException se nenhum contrato for encontrado para o accountId informado.
     * @throws IllegalStateException se o novo valor do limite for inferior ao saldo devedor atual.
     */
    public ContratoResponseDTO alterarLimite(String accountId, BigDecimal novoValorLimite) {
        Contrato contrato = findContratoByAccountId(accountId);

        if (novoValorLimite.compareTo(contrato.getSaldoDevedor()) < 0) {
            throw new IllegalStateException("O valor do limite não pode ser inferior ao saldo devedor atual.");
        }

        contrato.setValorLimite(novoValorLimite);
        
        Contrato savedContrato = contratoRepository.save(contrato);
        return toResponseDTO(savedContrato);
    }

    /**
     * Cancela (remove) um contrato de limite.
     * O cancelamento só é permitido se o saldo devedor for zero.
     *
     * @param accountId O identificador da conta.
     * @throws ResourceNotFoundException se nenhum contrato for encontrado para o accountId informado.
     * @throws IllegalStateException se o contrato possuir saldo devedor maior que zero.
     */
    public void cancelarContrato(String accountId) {
        Contrato contrato = findContratoByAccountId(accountId);
        if (contrato.getSaldoDevedor().compareTo(BigDecimal.ZERO) > 0) {
            throw new IllegalStateException("Não é possível cancelar um contrato com saldo devedor positivo.");
        }
        contratoRepository.delete(contrato);
    }

    /**
     * Registra um débito em um contrato, aumentando o saldo devedor.
     * A operação é negada se o débito ultrapassar o limite disponível.
     *
     * @param accountId O identificador da conta.
     * @param valor O valor a ser debitado.
     * @return O DTO de resposta com os dados do contrato atualizado.
     * @throws ResourceNotFoundException se nenhum contrato for encontrado para o accountId informado.
     * @throws IllegalStateException se o valor do débito for maior que o limite disponível.
     */
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

    /**
     * Registra um crédito em um contrato, reduzindo o saldo devedor.
     * O saldo devedor nunca se tornará negativo; o valor mínimo é zero.
     *
     * @param accountId O identificador da conta.
     * @param valor O valor a ser creditado.
     * @return O DTO de resposta com os dados do contrato atualizado.
     * @throws ResourceNotFoundException se nenhum contrato for encontrado para o accountId informado.
     */
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
