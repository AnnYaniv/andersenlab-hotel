package com.andersenlab.springinterface.controller;

import com.andersenlab.hotel.model.Client;
import com.andersenlab.hotel.model.ClientEntity;
import com.andersenlab.hotel.model.ClientSort;
import com.andersenlab.hotel.service.CrudService;
import com.andersenlab.hotel.usecase.CalculateClientStayCurrentPriceUseCase;
import com.andersenlab.hotel.usecase.CheckInClientUseCase;
import com.andersenlab.hotel.usecase.CheckOutClientUseCase;
import com.andersenlab.hotel.usecase.ListClientsUseCase;
import com.andersenlab.springinterface.dto.ClientDto;
import com.andersenlab.springinterface.model.ClientCases;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/clients")
public class ClientController {
    private final CrudService<Client, ClientEntity> clientService;
    private final ListClientsUseCase listClientsUseCase;
    private final CheckInClientUseCase checkInClientUseCase;
    private final CheckOutClientUseCase checkOutClientUseCase;
    private final CalculateClientStayCurrentPriceUseCase calculateClientStayCurrentPriceUseCase;

    @Autowired
    public ClientController(ClientCases clientCases) {
        clientService = clientCases.clientService();
        listClientsUseCase = clientCases.listClientsUseCase();
        checkInClientUseCase = clientCases.checkInClientUseCase();
        checkOutClientUseCase = clientCases.checkOutClientUseCase();
        calculateClientStayCurrentPriceUseCase = clientCases.calculateClientStayCurrentPriceUseCase();
    }

    @GetMapping
    public List<ClientEntity> getAll(@RequestParam String sort) {
        ClientSort clientSort = EnumUtils.getEnum(ClientSort.class, sort.toUpperCase());
        return listClientsUseCase.list(clientSort);
    }

    @PostMapping
    public ClientEntity create(@RequestBody ClientDto clientDto) {
        Client client = clientDto.getClient();
        clientService.save(client);

        return clientService.getById(client.getId());
    }

    @GetMapping("/{id}")
    public ClientEntity getById(@PathVariable String id) {
        return clientService.getById(UUID.fromString(id));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        clientService.delete(UUID.fromString(id));
    }

    @PutMapping("/check-in")
    public ClientEntity checkIn(@RequestBody String clientId, @RequestBody String apartmentId) {
        checkInClientUseCase.checkIn(
                UUID.fromString(clientId),
                UUID.fromString(apartmentId)
        );
        return clientService.getById(UUID.fromString(clientId));
    }

    @PutMapping("/check-out")
    public ClientEntity checkOut(@RequestBody String clientId, @RequestBody String apartmentId) {
        checkOutClientUseCase.checkOut(
                UUID.fromString(clientId),
                UUID.fromString(apartmentId)
        );
        return clientService.getById(UUID.fromString(clientId));
    }

    @GetMapping("/stay")
    public BigDecimal calculatePrice(@RequestParam String clientId) {
        return calculateClientStayCurrentPriceUseCase.calculatePrice(UUID.fromString(clientId));
    }
}
