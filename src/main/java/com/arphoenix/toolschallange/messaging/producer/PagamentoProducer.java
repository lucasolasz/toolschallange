package com.arphoenix.toolschallange.messaging.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.arphoenix.toolschallange.domain.records.PagamentoRequestRecord;

@Service
public class PagamentoProducer {

    private final KafkaTemplate<String, PagamentoRequestRecord> kafkaTemplate;

    public PagamentoProducer(KafkaTemplate<String, PagamentoRequestRecord> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendPagamento(PagamentoRequestRecord pagamento) {
        kafkaTemplate.send("vendas-pendentes", pagamento);
    }

}