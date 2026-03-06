package com.arphoenix.toolschallange.messaging.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic vendasPendentesTopic() {
        return TopicBuilder.name("vendas-pendentes")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic vendasFinalizadasTopic() {
        return TopicBuilder.name("vendas-finalizadas")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
