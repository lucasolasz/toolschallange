package com.arphoenix.toolschallange.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.arphoenix.toolschallange.domain.entities.Transacao;
import com.arphoenix.toolschallange.domain.mappers.TransacaoMapper;
import com.arphoenix.toolschallange.domain.records.PagamentoResponseRecord;
import com.arphoenix.toolschallange.domain.repositories.TransacaoRepository;
import com.arphoenix.toolschallange.exception.NotFoundException;

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

    public PagamentoResponseRecord recuperarPorId(String id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        return transacaoRepository.findById(id).map(mapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Transação com ID " + id + " não encontrada."));
    }

    public Transacao gravar(Transacao transacao) {
        if (transacao == null) {
            throw new IllegalArgumentException("Transação não pode ser nula");
        }
        return transacaoRepository.save(transacao);
    }

}
