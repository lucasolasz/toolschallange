package com.arphoenix.toolschallange.domain.enums;

public enum TipoPagamento {
    AVISTA, PARCELADO_LOJA, PARCELADO_EMISSOR;

    public boolean isParcelado() {
        return this != AVISTA;
    }
}
