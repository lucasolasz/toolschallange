package com.arphoenix.toolschallange.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import com.arphoenix.toolschallange.domain.entities.Transacao;
import com.arphoenix.toolschallange.domain.mappers.TransacaoMapper;
import com.arphoenix.toolschallange.domain.records.PagamentoRequestRecord;
import com.arphoenix.toolschallange.domain.records.PagamentoResponseRecord;
import com.arphoenix.toolschallange.domain.repositories.TransacaoRepository;
import com.arphoenix.toolschallange.exception.NotFoundException;
import com.arphoenix.toolschallange.messaging.producer.PagamentoProducer;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PagamentoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PagamentoService.class);
    private final TransacaoRepository transacaoRepository;
    private final TransacaoMapper mapper;
    private final PagamentoProducer pagamentoProducer;

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

    public PagamentoResponseRecord processarPagamento(PagamentoRequestRecord request) {
        LOGGER.info("Processando pagamento para ID: {}", request.transacao().id());

        validarRequest(request);

        Transacao transacao = mapper.toEntity(request);
        transacao = transacaoRepository.save(transacao);

        pagamentoProducer.sendPagamento(request);

        LOGGER.info("Pagamento processado e enviado para tópico vendas-pendentes: {}", request.transacao().id());
        return mapper.toResponse(transacao);
    }

    private void validarRequest(PagamentoRequestRecord request) {
        if (request == null) {
            throw new IllegalArgumentException("PagamentoRequestRecord não pode ser nulo");
        }

        // Validação de unicidade do ID
        if (verificaSeIdExiste(request.transacao().id())) {
            throw new IllegalArgumentException("Transação com ID " + request.transacao().id() + " já existe.");
        }

        // Validação de dataHora não futura
        if (request.transacao().descricao().dataHora().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("A data e hora da transação não pode ser no futuro.");
        }
    }

    private boolean verificaSeIdExiste(@NonNull String id) {
        return transacaoRepository.existsById(id);
    }

}
