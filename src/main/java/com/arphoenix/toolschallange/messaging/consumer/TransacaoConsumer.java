package com.arphoenix.toolschallange.messaging.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.arphoenix.toolschallange.domain.records.PagamentoResponseRecord;
import com.arphoenix.toolschallange.service.PagamentoService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransacaoConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransacaoConsumer.class);
    private final PagamentoService pagamentoService;

    @KafkaListener(topics = "vendas-finalizadas", groupId = "kafka-desafio-arphoenix")
    public void consumirFinalizacaoDePagamento(PagamentoResponseRecord response) {
        LOGGER.info("Recebendo confirmação de finalização para transação ID: {}", response.transacao().id());
        PagamentoResponseRecord responseFinalizado = pagamentoService.finalizarTransacao(response);

        // Implementação para "acordar" o Service, ou seja, liberar a resposta para o
        // endpoint que está aguardando. Adaptação para simular o comportamento de um
        // sistema real, onde o Service aguardaria a resposta do Consumer para liberar a
        // resposta do endpoint.
        pagamentoService.liberarResposta(responseFinalizado);
    }
}