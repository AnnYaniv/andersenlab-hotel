package com.andersenlab.kafkaconsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;

import java.util.UUID;

public class Consumer {
    private static final Logger LOG = LoggerFactory.getLogger(Consumer.class);

    @KafkaListener(topics = "${apache-kafka-consumer.kafka.topic}")
    void listenCheckIn(UUID apartmentId) {
        LOG.info("CheckIn into apartment: {}", apartmentId);
    }
}
