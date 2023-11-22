package com.andersenlab.springinterface.model;

import com.andersenlab.hotel.model.Client;
import com.andersenlab.hotel.model.ClientEntity;
import com.andersenlab.hotel.service.CrudService;
import com.andersenlab.hotel.usecase.CalculateClientStayCurrentPriceUseCase;
import com.andersenlab.hotel.usecase.CheckInClientUseCase;
import com.andersenlab.hotel.usecase.CheckOutClientUseCase;
import com.andersenlab.hotel.usecase.ListClientsUseCase;

public record ClientCases(CrudService<Client, ClientEntity> clientService, ListClientsUseCase listClientsUseCase,
                   CheckInClientUseCase checkInClientUseCase, CheckOutClientUseCase checkOutClientUseCase,
                   CalculateClientStayCurrentPriceUseCase calculateClientStayCurrentPriceUseCase) {
}
