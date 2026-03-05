package com.arphoenix.toolschallange.domain.mappers;

import org.mapstruct.Mapping;

import com.arphoenix.toolschallange.domain.entities.Transacao;
import com.arphoenix.toolschallange.domain.records.PagamentoResponseRecord;

public interface TransacaoMapper {

    @Mapping(source = "id", target = "transacao.id")
    @Mapping(source = "cartao", target = "transacao.cartao")
    @Mapping(source = "descricao", target = "transacao.descricao")
    @Mapping(source = "formaPagamento", target = "transacao.formaPagamento")
    PagamentoResponseRecord toResponse(Transacao entity);

    @Mapping(target = "id", source = "transacao.id")
    @Mapping(target = "cartao", source = "transacao.cartao")
    @Mapping(target = "descricao", source = "transacao.descricao")
    @Mapping(target = "formaPagamento", source = "transacao.formaPagamento")
    Transacao toEntity(com.arphoenix.toolschallange.domain.records.PagamentoRequestRecord request);
}
