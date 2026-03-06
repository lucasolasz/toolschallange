package com.arphoenix.toolschallange.messaging.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.arphoenix.toolschallange.domain.entities.Transacao;
import com.arphoenix.toolschallange.domain.mappers.TransacaoMapper;
import com.arphoenix.toolschallange.domain.records.PagamentoResponseRecord;
import com.arphoenix.toolschallange.domain.repositories.TransacaoRepository;
import com.arphoenix.toolschallange.service.PagamentoService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransacaoConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransacaoConsumer.class);
    private final PagamentoService service;
    private final TransacaoRepository repository;
    private final TransacaoMapper mapper;

    @KafkaListener(topics = "vendas-finalizadas", groupId = "kafka-desafio-arphoenix")
    public void consumirFinalizacaoDePagamento(PagamentoResponseRecord response) {
        LOGGER.info("Recebendo confirmação de finalização para transação ID: {}", response.transacao().id());
        Transacao transacao = mapper.toEntityFromResponse(response);

        repository.save(transacao);
        service.liberarResposta(transacao);
    }
}