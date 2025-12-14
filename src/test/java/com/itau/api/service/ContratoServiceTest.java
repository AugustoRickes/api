package com.itau.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.itau.api.dto.ContratoRequestDTO;
import com.itau.api.exception.ResourceNotFoundException;
import com.itau.api.model.Contrato;
import com.itau.api.repository.ContratoRepository;

@ExtendWith(MockitoExtension.class)
class ContratoServiceTest {

    @Mock
    private ContratoRepository contratoRepository;

    @InjectMocks
    private ContratoService contratoService;

    private Contrato contrato;
    
    @BeforeEach
    void setUp() {
        contrato = Contrato.builder()
            .id(UUID.randomUUID())
            .accountId("9876-5")
            .valorLimite(new BigDecimal("1000.00"))
            .saldoDevedor(new BigDecimal("200.00"))
            .build();
    }

    @Test
    @DisplayName("Deve criar um contrato com sucesso quando não existir um para a conta")
    void criarContrato_Success() {
        // Arrange
        when(contratoRepository.findByAccountId(anyString())).thenReturn(Optional.empty());
        when(contratoRepository.save(any(Contrato.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ContratoRequestDTO request = new ContratoRequestDTO("9876-5", new BigDecimal("1000"));

        // Act
        var response = contratoService.criarContrato(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getAccountId()).isEqualTo("9876-5");
        assertThat(response.getSaldoDevedor()).isEqualByComparingTo(BigDecimal.ZERO);
        verify(contratoRepository).save(any(Contrato.class));
    }
     @Test
    @DisplayName("Deve lançar exceção ao tentar criar um contrato para uma conta que já possui um")
    void criarContrato_WhenAlreadyExists_ThrowsException() {
        // Arrange
        when(contratoRepository.findByAccountId("9876-5")).thenReturn(Optional.of(contrato));
        ContratoRequestDTO request = new ContratoRequestDTO("9876-5", new BigDecimal("1000"));

        // Act & Assert
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,() -> {
            contratoService.criarContrato(request);
        });
        assertThat(thrown.getMessage()).isEqualTo("Contrato já existente para este accountId.");
        
        verify(contratoRepository, never()).save(any(Contrato.class));
    }

    @Test
    @DisplayName("Deve registrar débito com sucesso quando o limite for suficiente")
    void registrarDebito_WithSufficientLimit_Success() {
        // Arrange
        when(contratoRepository.findByAccountId("9876-5")).thenReturn(Optional.of(contrato));
        when(contratoRepository.save(any(Contrato.class))).thenReturn(contrato);
        
        BigDecimal valorDebito = new BigDecimal("300.00");

        // Act
        var response = contratoService.registrarDebito("9876-5", valorDebito);

        // Assert
        assertThat(response.getSaldoDevedor()).isEqualByComparingTo("500.00");
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar registrar débito com limite insuficiente")
    void registrarDebito_WithInsufficientLimit_ThrowsException() {
        // Arrange
        when(contratoRepository.findByAccountId("9876-5")).thenReturn(Optional.of(contrato));
        BigDecimal valorDebito = new BigDecimal("900.00");

        // Act & Assert
        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> {
            contratoService.registrarDebito("9876-5", valorDebito);
        });
        assertThat(thrown.getMessage()).isEqualTo("Débito não permitido. Limite disponível insuficiente.");
    }

    @Test
    @DisplayName("Deve registrar crédito e reduzir o saldo devedor")
    void registrarCredito_ReducesSaldoDevedor() {
        // Arrange
        when(contratoRepository.findByAccountId("9876-5")).thenReturn(Optional.of(contrato));
        when(contratoRepository.save(any(Contrato.class))).thenReturn(contrato);
        
        BigDecimal valorCredito = new BigDecimal("150.00");

        // Act
        var response = contratoService.registrarCredito("9876-5", valorCredito);

        // Assert
        assertThat(response.getSaldoDevedor()).isEqualByComparingTo("50.00");
    }

    @Test
    @DisplayName("Deve zerar o saldo devedor se o crédito for maior que o saldo atual")
    void registrarCredito_WhenSaldoDevedorGoesNegative_SetsToZero() {
        // Arrange
        when(contratoRepository.findByAccountId("9876-5")).thenReturn(Optional.of(contrato));
        when(contratoRepository.save(any(Contrato.class))).thenReturn(contrato);
        
        BigDecimal valorCredito = new BigDecimal("300.00");

        // Act
        var response = contratoService.registrarCredito("9876-5", valorCredito);

        // Assert
        assertThat(response.getSaldoDevedor()).isEqualByComparingTo(BigDecimal.ZERO);
    }
    
    @Test
    @DisplayName("Deve lançar exceção ao consultar um contrato inexistente")
    void consultarContrato_WhenNotFound_ThrowsException() {
        // Arrange
        when(contratoRepository.findByAccountId(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException thrown = assertThrows(ResourceNotFoundException.class, () -> {
            contratoService.consultarContrato("99887766-55");
        });
        assertThat(thrown.getMessage()).isEqualTo("Contrato não encontrado para o accountId: 99887766-55");
    }
    
    @Test
    @DisplayName("Deve consultar um contrato com sucesso")
    void consultarContrato_Success() {
        // Arrange
        when(contratoRepository.findByAccountId("9876-5")).thenReturn(Optional.of(contrato));

        // Act
        var response = contratoService.consultarContrato("9876-5");

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getAccountId()).isEqualTo("9876-5");
        assertThat(response.getValorLimite()).isEqualByComparingTo("1000.00");
        assertThat(response.getSaldoDevedor()).isEqualByComparingTo("200.00");
        assertThat(response.getLimiteDisponivel()).isEqualByComparingTo("800.00");
    }

    @Test
    @DisplayName("Deve alterar o limite do contrato com sucesso")
    void alterarLimite_Success() {
        // Arrange
        when(contratoRepository.findByAccountId("9876-5")).thenReturn(Optional.of(contrato));
        when(contratoRepository.save(any(Contrato.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        BigDecimal novoLimite = new BigDecimal("1500.00");

        // Act
        var response = contratoService.alterarLimite("9876-5", novoLimite);

        // Assert
        assertThat(response.getValorLimite()).isEqualByComparingTo(novoLimite);
        assertThat(response.getLimiteDisponivel()).isEqualByComparingTo("1300.00");
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar alterar limite para um valor inferior ao saldo devedor")
    void alterarLimite_WhenNewLimitIsLessThanSaldoDevedor_ThrowsException() {
        // Arrange
        when(contratoRepository.findByAccountId("9876-5")).thenReturn(Optional.of(contrato));
        
        BigDecimal novoLimite = new BigDecimal("100.00");
        // Act & Assert
        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> {
            contratoService.alterarLimite("9876-5", novoLimite);
        });
        assertThat(thrown.getMessage()).isEqualTo("O valor do limite não pode ser inferior ao saldo devedor atual.");
    }

    @Test
    @DisplayName("Deve cancelar contrato com sucesso quando saldo devedor for zero")
    void cancelarContrato_Success() {
        // Arrange
        contrato.setSaldoDevedor(BigDecimal.ZERO);
        when(contratoRepository.findByAccountId("9876-5")).thenReturn(Optional.of(contrato));

        // Act
        contratoService.cancelarContrato("9876-5");

        // Assert
        verify(contratoRepository).delete(contrato);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar cancelar contrato com saldo devedor positivo")
    void cancelarContrato_WhenSaldoDevedorPositive_ThrowsException() {
        // Arrange
        when(contratoRepository.findByAccountId("9876-5")).thenReturn(Optional.of(contrato));
        
        // Act & Assert
        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> {
            contratoService.cancelarContrato("9876-5");
        });
        assertThat(thrown.getMessage()).isEqualTo("Não é possível cancelar um contrato com saldo devedor positivo.");
    }
}
