package com.andersenlab.kafkaspringbootstarter;

import com.andersenlab.hotel.service.MessageBroker;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import org.springframework.kafka.core.KafkaTemplate;

public class KafkaMessageBroker implements MessageBroker {

    private final KafkaTemplate<Void, UUID> kafkaTemplate;
    private final String topic;

    KafkaMessageBroker(KafkaTemplate<Void, UUID> kafkaTemplate, String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    private static final class CouldNotPublish extends RuntimeException {

        CouldNotPublish(Throwable cause) {
            super(cause);
        }
    }
    @Override
    public void publishCheckedInApartment(UUID apartmentId) {
        try {
            kafkaTemplate.send(topic, apartmentId).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CouldNotPublish(e);
        } catch (ExecutionException e) {
            throw new CouldNotPublish(e);
        }
    }
}
