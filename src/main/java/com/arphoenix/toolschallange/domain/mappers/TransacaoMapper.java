package com.arphoenix.toolschallange.domain.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.arphoenix.toolschallange.domain.entities.Transacao;
import com.arphoenix.toolschallange.domain.records.PagamentoRequestRecord;
import com.arphoenix.toolschallange.domain.records.PagamentoResponseRecord;

@Mapper(componentModel = "spring")
public interface TransacaoMapper {

    @Mapping(target = "transacao", source = ".")
    PagamentoResponseRecord toResponse(Transacao entity);

    @Mapping(target = ".", source = "transacao")
    @Mapping(target = "descricao.nsu", ignore = true)
    @Mapping(target = "descricao.codigoAutorizacao", ignore = true)
    @Mapping(target = "descricao.status", ignore = true)
    Transacao toEntity(PagamentoRequestRecord request);

    @Mapping(target = ".", source = "transacao")
    Transacao toEntityFromResponse(PagamentoResponseRecord response);
}
