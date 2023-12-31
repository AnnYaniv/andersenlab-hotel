package com.andersenlab.hotel.usecase;

import com.andersenlab.hotel.model.ClientEntity;
import com.andersenlab.hotel.model.ClientSort;

import java.util.List;

public interface ListClientsUseCase {

    List<ClientEntity> list(ClientSort sort);
}
