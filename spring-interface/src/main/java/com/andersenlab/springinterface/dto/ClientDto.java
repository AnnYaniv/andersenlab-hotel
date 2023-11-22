package com.andersenlab.springinterface.dto;

import com.andersenlab.hotel.model.Client;
import com.andersenlab.hotel.model.ClientStatus;
import org.apache.commons.lang3.EnumUtils;

import java.util.UUID;

public record ClientDto(String id, String name, String status) {
    public Client getClient() {
        return new Client(UUID.fromString(id), name,
                EnumUtils.getEnum(ClientStatus.class, status.toUpperCase()));
    }
}
