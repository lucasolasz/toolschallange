package com.arphoenix.toolschallange.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import com.arphoenix.toolschallange.domain.entities.Transacao;
import com.arphoenix.toolschallange.domain.enums.StatusTransacao;
import com.arphoenix.toolschallange.domain.enums.TipoPagamento;
import com.arphoenix.toolschallange.domain.mappers.TransacaoMapper;
import com.arphoenix.toolschallange.domain.records.PagamentoRequestRecord;
import com.arphoenix.toolschallange.domain.records.PagamentoResponseRecord;
import com.arphoenix.toolschallange.domain.repositories.TransacaoRepository;
import com.arphoenix.toolschallange.exception.NotFoundException;
import com.arphoenix.toolschallange.exception.TempoExcedidoRequisicaoException;
import com.arphoenix.toolschallange.messaging.producer.PagamentoProducer;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PagamentoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PagamentoService.class);
    private final TransacaoRepository transacaoRepository;
    private final TransacaoMapper mapper;
    private final PagamentoProducer pagamentoProducer;

    private final Map<String, CompletableFuture<PagamentoResponseRecord>> espera = new ConcurrentHashMap<>();

    public List<PagamentoResponseRecord> recuperarTodos() {
        return transacaoRepository.findAll().stream()
                .map(mapper::toResponse)
                .toList();
    }

    public PagamentoResponseRecord recuperarPorId(String id) {
        return transacaoRepository.findById(this.validarERetornarId(id))
                .map(mapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Transação " + id + " não encontrada."));
    }

    @Transactional
    public PagamentoResponseRecord estornar(String id) {
        Transacao transacao = transacaoRepository.findById(this.validarERetornarId(id))
                .orElseThrow(() -> new NotFoundException("Transação " + id + " não encontrada."));

        this.validarEstorno(transacao);

        transacao.getDescricao().setStatus(StatusTransacao.CANCELADO);
        transacao = transacaoRepository.save(transacao);

        LOGGER.info("Transação {} estornada com status CANCELADO", transacao.getId());

        return mapper.toResponse(transacao);
    }

    private void validarEstorno(Transacao transacao) {
        StatusTransacao status = transacao.getDescricao().getStatus();
        if (status == StatusTransacao.CANCELADO) {
            throw new IllegalArgumentException("Não é possível estornar uma transação já cancelada.");
        }
        if (status == StatusTransacao.NEGADO) {
            throw new IllegalArgumentException("Não é possível estornar uma transação negada.");
        }
    }

    @Transactional
    public PagamentoResponseRecord finalizarTransacao(PagamentoResponseRecord response) {

        Transacao transacao = mapper.toEntityFromResponse(response);

        transacao = this.gravar(transacao);

        LOGGER.info("Transação {} finalizada com NSU: {}, Código Autorização: {}, Status: {}",
                transacao.getId(), transacao.getDescricao().getNsu(), transacao.getDescricao().getCodigoAutorizacao(),
                transacao.getDescricao().getStatus());

        return response;
    }

    @Transactional
    public Transacao gravar(Transacao transacao) {
        if (transacao == null) {
            throw new IllegalArgumentException("Transação não pode ser nula");
        }
        return transacaoRepository.save(transacao);
    }

    public PagamentoResponseRecord processarPagamento(PagamentoRequestRecord request) {
        this.validarCamposRequestProcessamentoAntesDePersistir(request);

        String requestId = request.transacao().id();
        LOGGER.info("Processando pagamento para ID: {}", requestId);

        CompletableFuture<PagamentoResponseRecord> promessa = new CompletableFuture<>();
        espera.put(requestId, promessa);

        this.pagamentoProducer.enviarPagamentoParaTopico(request);
        LOGGER.info("Pagamento processado e enviado para tópico vendas-pendentes:  {}", request.transacao().id());

        // Simulacao de espera ativa, aguardando o microservico de autorizacao processar
        // e enviar a resposta para o tópico "vendas-finalizadas"
        try {
            PagamentoResponseRecord responseFinalizada = promessa.get(4, TimeUnit.SECONDS);
            LOGGER.info("Resposta recebida para transação {}", requestId);
            return responseFinalizada;

        } catch (TimeoutException | TempoExcedidoRequisicaoException e) {
            LOGGER.warn("Timeout aguardando autorização para transação {}", requestId);
            throw new TempoExcedidoRequisicaoException(
                    "O sistema de autorização demorou a responder. Tente novamente mais tarde.");
        } catch (ExecutionException | InterruptedException e) {
            LOGGER.error("Erro ao aguardar resposta da autorização", e);
            throw new RuntimeException("Erro ao processar autorização da transação");
        } finally {
            espera.remove(requestId);
        }

    }

    // Método que o Consumer vai chamar para "acordar" o Service
    public void liberarResposta(PagamentoResponseRecord response) {
        CompletableFuture<PagamentoResponseRecord> promessa = espera.get(response.transacao().id());
        if (promessa != null) {
            LOGGER.info("Liberando resposta da transação {}", response.transacao().id());
            promessa.complete(response);
        }
    }

    private void validarCamposRequestProcessamentoAntesDePersistir(PagamentoRequestRecord request) {

        if (request == null) {
            throw new IllegalArgumentException("Corpo da requisição não pode ser nulo");
        }

        String id = this.validarERetornarId(request.transacao().id());

        if (id != null && verificaSeIdExiste(id)) {
            throw new IllegalArgumentException(
                    "Transação com ID " + id + " já existe.");
        }

        if (request.transacao().descricao().dataHora().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException(
                    "A data e hora da transação não pode ser no futuro.");
        }

        this.validarRegraParcelas(request);
    }

    private boolean verificaSeIdExiste(@NonNull String id) {
        return transacaoRepository.existsById(id);
    }

    private @NonNull String validarERetornarId(String id) {
        if (id == null || id.isBlank())
            throw new IllegalArgumentException("ID não pode ser nulo");
        try {
            if (Long.parseLong(id) <= 0)
                throw new IllegalArgumentException("ID deve ser positivo");
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("ID deve ser numérico");
        }
        return id;
    }

    private void validarRegraParcelas(PagamentoRequestRecord request) {
        var tipo = request.transacao().formaPagamento().tipo();
        var parcelas = request.transacao().formaPagamento().parcelas();

        boolean invalidoAvista = (tipo == TipoPagamento.AVISTA && parcelas != 1);
        boolean invalidoParcelado = (tipo.isParcelado() && parcelas < 2);

        if (invalidoAvista || invalidoParcelado) {
            throw new IllegalArgumentException("Número de parcelas incompatível com tipo de pagamento");
        }
    }

}
