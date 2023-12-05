package com.andersenlab.kafkaspringbootstarter;

import com.andersenlab.hotel.service.MessageBroker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;

@AutoConfiguration
public class KafkaAutoconfiguration {

    @Bean
    public MessageBroker messageBroker(KafkaProperties kafkaProperties, @Value("${hotel.kafka.topic}") String topic) {
        return new KafkaMessageBroker(
                new KafkaTemplate<>(
                        new DefaultKafkaProducerFactory<>(
                                kafkaProperties.buildProducerProperties()
                        )
                ),
                topic
        );
    }
}
