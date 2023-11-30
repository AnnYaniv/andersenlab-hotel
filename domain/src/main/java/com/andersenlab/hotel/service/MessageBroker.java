package com.andersenlab.hotel.service;

import java.util.UUID;

public interface MessageBroker {

    void publishCheckedInApartment(UUID apartmentId);
}
