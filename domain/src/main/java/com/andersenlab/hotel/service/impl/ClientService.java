package com.andersenlab.hotel.service.impl;

import com.andersenlab.hotel.model.Apartment;
import com.andersenlab.hotel.model.ApartmentEntity;
import com.andersenlab.hotel.model.ApartmentStatus;
import com.andersenlab.hotel.model.Client;
import com.andersenlab.hotel.model.ClientEntity;
import com.andersenlab.hotel.model.ClientSort;
import com.andersenlab.hotel.model.ClientStatus;
import com.andersenlab.hotel.repository.SortableCrudRepository;
import com.andersenlab.hotel.service.CrudService;
import com.andersenlab.hotel.usecase.CalculateClientStayCurrentPriceUseCase;
import com.andersenlab.hotel.usecase.CheckInClientUseCase;
import com.andersenlab.hotel.usecase.CheckOutClientUseCase;
import com.andersenlab.hotel.usecase.ListClientsUseCase;
import com.andersenlab.hotel.usecase.exception.ApartmentReservedException;
import com.andersenlab.hotel.usecase.exception.ClientBannedException;
import com.andersenlab.hotel.usecase.exception.ClientIsAlreadyExistsException;
import com.andersenlab.hotel.usecase.exception.ClientNotfoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class ClientService implements CalculateClientStayCurrentPriceUseCase,
        CheckInClientUseCase, CheckOutClientUseCase, ListClientsUseCase,
        CrudService<Client, ClientEntity> {
    private static final Logger LOG = LoggerFactory.getLogger(ClientService.class);
    private final SortableCrudRepository<Client, ClientSort> store;
    private final ApartmentService apartmentService;

    public ClientService(final SortableCrudRepository<Client, ClientSort> store, final ApartmentService apartmentService) {
        this.store = store;
        this.apartmentService = apartmentService;
    }

    @Override
    public BigDecimal calculatePrice(UUID id) {
        LOG.info("Calculate client price. ID: {}", id);
        return getById(id)
                .apartments()
                .stream()
                .map(ApartmentEntity::price)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
    }

    @Override
    public void checkIn(UUID clientId, UUID apartmentId) {
        ClientEntity client = getById(clientId);
        if (client.status().equals(ClientStatus.BANNED)) {
            throw new ClientBannedException();

        }
        ApartmentEntity apartment = apartmentService.getById(apartmentId);
        if (apartment.status().equals(ApartmentStatus.RESERVED)) {
            throw new ApartmentReservedException();
        }

        apartmentService.update(
                new Apartment(apartment.id(), apartment.price(), apartment.capacity(),
                        false,
                        ApartmentStatus.RESERVED)
        );
        client.apartments().add(apartmentService.getById(apartmentId));
        update(toClientMapper(client));
        LOG.info("Check in client. Client ID: {}, Apartment ID: {}", clientId, apartmentId);
    }

    @Override
    public List<ClientEntity> list(ClientSort sort) {
        return store.findAllSorted(sort)
                .stream()
                .map(this::toEntityMapper)
                .toList();
    }

    @Override
    public void checkOut(UUID clientId, UUID apartmentId) {
        ClientEntity client = getById(clientId);
        ApartmentEntity apartment = apartmentService.getById(apartmentId);

        Optional<ApartmentEntity> apartmentEntity = client.apartments().stream()
                .filter(a -> a.id().equals(apartmentId)).findFirst();
        apartmentEntity.ifPresent(a -> client.apartments().remove(a));

        boolean ifClientHasApartment = apartmentEntity.isPresent();
        if (ifClientHasApartment) {
            apartmentService.update(
                    new Apartment(apartment.id(), apartment.price(), apartment.capacity(), true,
                            ApartmentStatus.AVAILABLE)
            );
        }
        update(toClientMapper(client));
        LOG.info("Check out client. Client ID: {}, Apartment ID: {}", clientId, apartmentId);
    }

    @Override
    public ClientEntity getById(UUID id) throws ClientNotfoundException {
        return store.getById(id)
                .map(this::toEntityMapper)
                .orElseThrow(ClientNotfoundException::new);
    }

    @Override
    public boolean has(UUID id) {
        return store.has(id);
    }

    @Override
    public void save(Client client) {
        if (has(client.getId())) {
            throw new ClientIsAlreadyExistsException();
        }
        UUID id = client.getId();

        store.save(client);
        LOG.info("Register new client. ID: {}", id);
    }

    @Override
    public void delete(UUID id) {
        if (!has(id)) {
            throw new ClientNotfoundException();
        }
        store.delete(id);
        LOG.info("Delete client. ID: {}", id);
    }

    @Override
    public void update(Client client) {
        if (!has(client.getId())) {
            throw new ClientNotfoundException();
        }
        store.update(client);
        LOG.info("Update client. ID: {}", client.getId());
    }

    private ClientEntity toEntityMapper(Client client) {
        return new ClientEntity(client.getId(), client.getName(), client.getStatus(), client.getApartments());
    }

    private Client toClientMapper(ClientEntity client) {
        return new Client(client.id(), client.name(), client.status(), client.apartments());
    }
}
