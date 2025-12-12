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
}
