package com.arphoenix.toolschallange.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.arphoenix.toolschallange.domain.entities.Transacao;
import com.arphoenix.toolschallange.domain.repositories.TransacaoRepository;

@Service
public class PagamentoService {

    private final TransacaoRepository transacaoRepository;

    public PagamentoService(TransacaoRepository transacaoRepository) {
        this.transacaoRepository = transacaoRepository;
    }

    public List<Transacao> recuperarTodos() {
        return transacaoRepository.findAll();
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
