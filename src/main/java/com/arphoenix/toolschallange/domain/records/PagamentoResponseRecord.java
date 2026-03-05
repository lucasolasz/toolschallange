package com.arphoenix.toolschallange.domain.records;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.arphoenix.toolschallange.domain.enums.StatusTransacao;
import com.arphoenix.toolschallange.domain.enums.TipoPagamento;
import com.fasterxml.jackson.annotation.JsonFormat;

public record PagamentoResponseRecord(TransacaoOutput transacao) {

    public record TransacaoOutput(
            String id,
            String cartao,
            DescricaoOutput descricao,
            FormaPagamentoOutput formaPagamento) {
    }

    public record DescricaoOutput(
            BigDecimal valor,
            @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss") LocalDateTime dataHora,
            String estabelecimento,
            String nsu,
            String codigoAutorizacao,
            StatusTransacao status) {
    }

    public record FormaPagamentoOutput(
            TipoPagamento tipo,
            Integer parcelas) {
    }
}
