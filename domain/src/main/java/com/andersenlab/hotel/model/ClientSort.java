package com.andersenlab.hotel.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Comparator;

@AllArgsConstructor
@Getter
public enum ClientSort {
    ID(Comparator.comparing(Client::getId)),
    NAME(Comparator.comparing(Client::getName)),
    STATUS(Comparator.comparing(client -> client.getStatus().name()));

    private final Comparator<Client> comparator;
}
