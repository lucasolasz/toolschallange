package com.arphoenix.toolschallange.domain.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.arphoenix.toolschallange.domain.enums.StatusTransacao;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class Descricao {

    @Column(precision = 15, scale = 2)
    private BigDecimal valor;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime dataHora;

    private String estabelecimento;

    private String nsu;

    private String codigoAutorizacao;

    @Enumerated(EnumType.STRING)
    private StatusTransacao status;
}
