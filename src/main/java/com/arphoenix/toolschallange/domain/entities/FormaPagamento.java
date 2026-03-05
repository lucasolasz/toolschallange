package com.arphoenix.toolschallange.domain.entities;

import com.arphoenix.toolschallange.domain.enums.TipoPagamento;

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
public class FormaPagamento {

    @Enumerated(EnumType.STRING)
    private TipoPagamento tipo;

    private Integer parcelas;
}
