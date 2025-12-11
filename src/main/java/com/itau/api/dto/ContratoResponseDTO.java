package com.itau.api.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContratoResponseDTO {
    private String accountId;
    private BigDecimal valorLimite;
    private BigDecimal saldoDevedor;
    private BigDecimal limiteDisponivel;
}
