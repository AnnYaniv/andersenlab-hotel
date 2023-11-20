package com.andersenlab.hotel.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
public final class Apartment {
    @Id
    private UUID id;
    private BigDecimal price;
    private BigInteger capacity;
    private boolean availability;
    @Enumerated(EnumType.STRING)
    private ApartmentStatus status;

    public Apartment(UUID id, BigDecimal price, BigInteger capacity, boolean availability) {
        this(id, price, capacity, availability, ApartmentStatus.AVAILABLE);
    }

    public Apartment(UUID id) {
        this(id, BigDecimal.ZERO, BigInteger.ZERO, false, ApartmentStatus.RESERVED);
    }

    public Apartment(UUID id, BigDecimal price, BigInteger capacity, boolean availability, ApartmentStatus status) {
        this.id = id;
        this.price = price;
        this.capacity = capacity;
        this.availability = availability;
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Apartment apartment = (Apartment) o;
        return availability == apartment.availability && Objects.equals(id, apartment.id) && Objects.equals(price, apartment.price) && Objects.equals(capacity, apartment.capacity) && status == apartment.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, price, capacity, availability, status);
    }
}
