package com.andersenlab.springinterface.ApartmentDto;

import com.andersenlab.hotel.model.Apartment;
import com.andersenlab.hotel.model.ApartmentStatus;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.UUID;

public record ApartmentDto(UUID id, BigDecimal price, BigInteger capacity, boolean availability,
                           ApartmentStatus status) {
    public Apartment getApartment() {
        return new Apartment(id, price, capacity, availability, status);
    }
}
