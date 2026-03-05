package com.arphoenix.toolschallange.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.arphoenix.toolschallange.domain.entities.Transacao;
import com.arphoenix.toolschallange.domain.mappers.TransacaoMapper;
import com.arphoenix.toolschallange.domain.records.PagamentoResponseRecord;
import com.arphoenix.toolschallange.domain.repositories.TransacaoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PagamentoService {

    private final TransacaoRepository transacaoRepository;
    private final TransacaoMapper mapper;

    public List<PagamentoResponseRecord> recuperarTodos() {
        return transacaoRepository.findAll().stream()
                .map(mapper::toResponse)
                .toList();
    }

    public Optional<Transacao> recuperarPorId(String id) {
        if (id == null) {
            return Optional.empty();
        }
        return transacaoRepository.findById(id);
    }

    public Transacao gravar(Transacao transacao) {
        if (transacao == null) {
            throw new IllegalArgumentException("Transação não pode ser nula");
        }
        return transacaoRepository.save(transacao);
    }

}
