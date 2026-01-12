package com.itau.api.mongo.model;

import com.itau.api.dto.TipoMovimentacao;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "movimentacoes")
public class MovimentacaoMongo {
    @Id
    private String id;
    private String accountId;
    private BigDecimal valor;
    private TipoMovimentacao tipo;
    private LocalDateTime timestamp;
}
