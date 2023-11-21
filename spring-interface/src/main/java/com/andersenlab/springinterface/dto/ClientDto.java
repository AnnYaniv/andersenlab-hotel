package com.andersenlab.springinterface.dto;

import com.andersenlab.hotel.model.Client;
import com.andersenlab.hotel.model.ClientStatus;
import lombok.Data;
import org.apache.commons.lang3.EnumUtils;

import java.util.UUID;

@Data
public class ClientDto {
    private String id;
    private String name;
    private String status;

    public Client getClient() {
        return new Client(UUID.fromString(id), name,
                EnumUtils.getEnum(ClientStatus.class, status.toUpperCase()));
    }
}
