package com.arphoenix.toolschallange.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

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
import com.arphoenix.toolschallange.exception.TempoExcedidoRequisicaoException;
import com.arphoenix.toolschallange.messaging.producer.PagamentoProducer;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PagamentoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PagamentoService.class);
    private final TransacaoRepository transacaoRepository;
    private final TransacaoMapper mapper;
    private final PagamentoProducer pagamentoProducer;

    private final Map<String, CompletableFuture<Transacao>> espera = new ConcurrentHashMap<>();

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

    // public void finalizarTransacao(PagamentoResponseRecord response) {
    // String id = response.transacao().id();
    // Transacao transacao = transacaoRepository.findById(id)
    // .orElseThrow(() -> new NotFoundException("Transação com ID " + id + " não
    // encontrada."));
    // preencheObjetoDoBancoComObjetoRespose(response, transacao);
    // transacaoRepository.save(transacao);
    // LOGGER.info("Transação {} finalizada com NSU: {}, Código Autorização: {},
    // Status: {}",
    // id, transacao.getNsu(), transacao.getCodigoAutorizacao(),
    // transacao.getStatus());
    // }

    // private void preencheObjetoDoBancoComObjetoRespose(PagamentoResponseRecord
    // response, Transacao transacao) {
    // transacao.setNsu(response.transacao().descricao().nsu());
    // transacao.setCodigoAutorizacao(response.transacao().descricao().codigoAutorizacao());
    // transacao.setStatus(response.transacao().descricao().status());

    // }

    public Transacao gravar(Transacao transacao) {
        if (transacao == null) {
            throw new IllegalArgumentException("Transação não pode ser nula");
        }
        return transacaoRepository.save(transacao);
    }

    public PagamentoResponseRecord processarPagamento(PagamentoRequestRecord request) {

        String requestId = request.transacao().id();
        LOGGER.info("Processando pagamento para ID: {}", requestId);

        CompletableFuture<Transacao> promessa = new CompletableFuture<>();
        espera.put(requestId, promessa);

        this.validarCamposRequestAntesDePersistir(request);

        this.pagamentoProducer.enviarPagamentoParaTopico(request);
        LOGGER.info("Pagamento processado e enviado para tópico vendas-pendentes:  {}", request.transacao().id());

        // Simulacao de espera ativa, aguardando o microservico de autorizacao processar
        // e enviar a resposta
        // para o tópico "vendas-finalizadas"
        try {
            Transacao transacaoFinalizada = promessa.get(4, TimeUnit.SECONDS);
            return mapper.toResponse(transacaoFinalizada);

        } catch (Exception e) {
            espera.remove(requestId);
            throw new TempoExcedidoRequisicaoException(
                    "O sistema de autorização demorou a responder. Tente novamente mais tarde.");
        } finally {
            espera.remove(requestId);
        }

        // Transacao transacao = mapper.toEntity(request);
        // transacao = transacaoRepository.save(transacao);

        // LOGGER.info("Pagamento processado e enviado para tópico vendas-pendentes:
        // {}", request.transacao().id());
        // return mapper.toResponse(transacao);
    }

    // Método que o Consumer vai chamar para "acordar" o Service
    public void liberarResposta(Transacao transacao) {
        CompletableFuture<Transacao> promessa = espera.get(transacao.getId());
        if (promessa != null) {
            promessa.complete(transacao); // Entrega o objeto e desbloqueia o GET lá em cima
        }
    }

    private void validarCamposRequestAntesDePersistir(PagamentoRequestRecord request) {
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
