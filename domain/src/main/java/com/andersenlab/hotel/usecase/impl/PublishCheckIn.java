package com.andersenlab.hotel.usecase.impl;

import com.andersenlab.hotel.service.MessageBroker;
import com.andersenlab.hotel.usecase.CheckInClientUseCase;

import java.util.UUID;

public class PublishCheckIn implements CheckInClientUseCase {

    private final CheckInClientUseCase checkInClientUseCase;
    private final MessageBroker messageBroker;

    public PublishCheckIn(CheckInClientUseCase checkInClientUseCase, MessageBroker messageBroker) {
        this.checkInClientUseCase = checkInClientUseCase;
        this.messageBroker = messageBroker;
    }

    @Override
    public void checkIn(UUID clientId, UUID apartmentId) {
        checkInClientUseCase.checkIn(clientId, apartmentId);
        messageBroker.publishCheckedInApartment(apartmentId);
    }
}
