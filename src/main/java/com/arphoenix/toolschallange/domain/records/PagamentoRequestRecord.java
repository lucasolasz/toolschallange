package com.arphoenix.toolschallange.domain.records;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.arphoenix.toolschallange.domain.enums.TipoPagamento;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public record PagamentoRequestRecord(
                @NotNull(message = "O objeto transacao é obrigatório") @Valid TransacaoInput transacao) {

        public record TransacaoInput(
                        @NotBlank(message = "O número do cartão é obrigatório") @Pattern(regexp = "\\d{4}\\*{8}\\d{4}", message = "O cartão deve estar no formato 4444********1234") String cartao,

                        @NotBlank(message = "O ID da transação é obrigatório") String id,

                        @NotNull(message = "Os detalhes da descrição são obrigatórios") @Valid DescricaoInput descricao,

                        @NotNull(message = "A forma de pagamento é obrigatória") @Valid FormaPagamentoInput formaPagamento) {
        }

        public record DescricaoInput(
                        @NotNull(message = "O valor é obrigatório") @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero") BigDecimal valor,

                        @NotNull(message = "A data e hora são obrigatórias") @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss") LocalDateTime dataHora,

                        @NotBlank(message = "O estabelecimento deve ser informado") String estabelecimento) {
        }

        public record FormaPagamentoInput(
                        @NotNull(message = "O tipo de pagamento (AVISTA, PARCELADO_LOJA, etc) é obrigatório") TipoPagamento tipo,

                        @NotNull(message = "O número de parcelas é obrigatório") @Positive(message = "O número de parcelas deve ser no mínimo 1") Integer parcelas) {
        }
}