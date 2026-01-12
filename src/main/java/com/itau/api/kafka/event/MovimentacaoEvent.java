package com.itau.api.kafka.event;

import com.itau.api.dto.TipoMovimentacao;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MovimentacaoEvent {
    private String accountId;
    private BigDecimal valor;
    private TipoMovimentacao tipo;
}
