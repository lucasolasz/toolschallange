package com.arphoenix.toolschallange.config;

import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.cfg.CoercionAction;
import com.fasterxml.jackson.databind.cfg.CoercionInputShape;
import com.fasterxml.jackson.databind.type.LogicalType;

@Configuration
public class JacksonConfig {

    /**
     * Permite que Jackson converta strings vazias para null quando o destino for um
     * enum.
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> builder.postConfigurer(mapper -> {
            mapper.coercionConfigFor(LogicalType.Enum)
                    .setCoercion(CoercionInputShape.EmptyString, CoercionAction.AsNull);
        });
    }
}
