package com.itau.api.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContratoRequestDTO {
    @NotBlank(message = "obrigatorio passar accountId.")
    private String accountId;

    @NotNull(message = "obrigatorio passar o valorLimite.")
    @Positive(message = "valorLimite deve ser positivo")
    private BigDecimal valorLimite;
}
