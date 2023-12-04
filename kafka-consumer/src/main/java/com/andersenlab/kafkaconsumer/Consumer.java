package com.andersenlab.kafkaconsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;

import java.util.UUID;

@KafkaListener(topics = "${apache-kafka-consumer.kafka.topic}")
public class Consumer {

    private static final Logger LOG = LoggerFactory.getLogger(Consumer.class);

    public Consumer() {
        LOG.info("Consumer created");
    }

    @KafkaHandler
    void listenCheckIn(UUID apartmentId) {
        LOG.info("CheckIn into apartment: {}", apartmentId);
    }
}
