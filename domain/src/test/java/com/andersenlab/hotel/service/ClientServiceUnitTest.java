package com.andersenlab.hotel.service;

import com.andersenlab.hotel.model.Apartment;
import com.andersenlab.hotel.model.ApartmentEntity;
import com.andersenlab.hotel.model.ApartmentStatus;
import com.andersenlab.hotel.model.Client;
import com.andersenlab.hotel.model.ClientEntity;
import com.andersenlab.hotel.model.ClientStatus;
import com.andersenlab.hotel.model.ClientSort;
import com.andersenlab.hotel.repository.inmemory.InMemoryClientRepository;
import com.andersenlab.hotel.service.impl.ApartmentService;
import com.andersenlab.hotel.service.impl.ClientService;
import com.andersenlab.hotel.usecase.exception.ApartmentNotfoundException;
import com.andersenlab.hotel.usecase.exception.ApartmentReservedException;
import com.andersenlab.hotel.usecase.exception.ClientBannedException;
import com.andersenlab.hotel.usecase.exception.ClientIsAlreadyExistsException;
import com.andersenlab.hotel.usecase.exception.ClientNotfoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ClientServiceUnitTest {

    private ClientService target;
    private InMemoryClientRepository repository;
    private ApartmentService apartmentService;
    private Client client;
    private ApartmentEntity apartmentEntity;

    @BeforeEach
    void setUp() {
        repository = mock(InMemoryClientRepository.class);
        apartmentService = mock(ApartmentService.class);
        target = new ClientService(repository, apartmentService);

        apartmentEntity = new ApartmentEntity(UUID.randomUUID(), BigDecimal.ONE, BigInteger.ONE,
                true, ApartmentStatus.AVAILABLE);
        Set<ApartmentEntity> apartments = new HashSet<>(
                Set.of(
                        apartmentEntity,
                        new ApartmentEntity(UUID.randomUUID(), BigDecimal.ONE, BigInteger.ONE,
                                true, ApartmentStatus.AVAILABLE),
                        new ApartmentEntity(UUID.randomUUID(), BigDecimal.TWO, BigInteger.ONE,
                                true, ApartmentStatus.AVAILABLE)
                )
        );
        client = new Client(UUID.randomUUID(), "name", ClientStatus.NEW, apartments);
    }

    @Test
    void calculatePrice_ExistingEntity_ShouldReturnDoubleValueOfSumPrice() {
        final UUID id = UUID.randomUUID();
        final BigDecimal expectedPrice = BigDecimal.valueOf(4);
        when(repository.getById(any(UUID.class)))
                .thenReturn(Optional.of(client));

        BigDecimal actual = target.calculatePrice(id);

        assertThat(actual).isEqualTo(expectedPrice);
    }

    @Test
    void calculatePrice_NonExistingEntity_ShouldThrowClientNotFoundException() {
        final UUID id = UUID.randomUUID();
        when(repository.getById(any(UUID.class)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                target.calculatePrice(id)
        ).isInstanceOf(ClientNotfoundException.class);
    }

    @Test
    void checkIn_NotExistingClient_ShouldThrowClientNotfoundException() {
        final UUID clientId = UUID.randomUUID();
        final UUID apartmentId = UUID.randomUUID();
        when(repository.getById(any(UUID.class)))
                .thenReturn(Optional.empty());
        when(apartmentService.getById(any(UUID.class)))
                .thenReturn(apartmentEntity);

        assertThatThrownBy(() ->
                target.checkIn(clientId, apartmentId)
        ).isInstanceOf(ClientNotfoundException.class);
    }

    @Test
    void checkIn_BannedClient_ShouldThrowClientBannedException() {
        final UUID clientId = UUID.randomUUID();
        final UUID apartmentId = UUID.randomUUID();
        client.setStatus(ClientStatus.BANNED);
        when(repository.getById(any(UUID.class)))
                .thenReturn(Optional.of(client));
        when(apartmentService.getById(any(UUID.class)))
                .thenReturn(apartmentEntity);

        assertThatThrownBy(() ->
                target.checkIn(clientId, apartmentId)
        ).isInstanceOf(ClientBannedException.class);
    }

    @Test
    void checkIn_ReservedApartment_ShouldThrowApartmentReservedException() {
        final UUID clientId = UUID.randomUUID();
        final UUID apartmentId = UUID.randomUUID();
        apartmentEntity = new ApartmentEntity(UUID.randomUUID(), BigDecimal.ONE, BigInteger.ONE,
                true, ApartmentStatus.RESERVED);
        when(repository.getById(any(UUID.class)))
                .thenReturn(Optional.of(client));
        when(apartmentService.getById(any(UUID.class)))
                .thenReturn(apartmentEntity);

        assertThatThrownBy(() ->
                target.checkIn(clientId, apartmentId)
        ).isInstanceOf(ApartmentReservedException.class);
    }

    @Test
    void checkIn_NotExistingApartment_ShouldThrowApartmentNotfoundException() {
        final UUID clientId = UUID.randomUUID();
        final UUID apartmentId = UUID.randomUUID();
        when(repository.getById(any(UUID.class)))
                .thenReturn(Optional.of(client));
        when(apartmentService.getById(any(UUID.class)))
                .thenThrow(ApartmentNotfoundException.class);

        assertThatThrownBy(() ->
                target.checkIn(clientId, apartmentId)
        ).isInstanceOf(ApartmentNotfoundException.class);
    }

    @Test
    void checkIn_ValidEntities_ShouldUpdateUserApartmentSet() {
        final UUID clientId = UUID.randomUUID();
        final UUID apartmentId = UUID.randomUUID();
        when(repository.getById(any(UUID.class)))
                .thenReturn(Optional.of(client));
        when(apartmentService.getById(any(UUID.class)))
                .thenReturn(apartmentEntity);
        when(repository.has(any(UUID.class)))
                .thenReturn(true);

        target.checkIn(clientId, apartmentId);

        verify(apartmentService).update(any(Apartment.class));
        assertThat(client.getApartments()).hasSize(3);
    }

    @Test
    void list_WithValidSortParam_ShouldCallRepositoryMethod() {
        ArgumentCaptor<ClientSort> captor = ArgumentCaptor.forClass(ClientSort.class);
        ClientSort sort = ClientSort.ID;

        target.list(sort);

        verify(repository).findAllSorted(captor.capture());
        assertThat(captor.getValue()).isEqualTo(sort);
    }

    @Test
    void checkOut_NotExistingClient_ShouldThrowClientNotfoundException() {
        final UUID clientId = UUID.randomUUID();
        final UUID apartmentId = UUID.randomUUID();
        when(repository.getById(any(UUID.class)))
                .thenReturn(Optional.empty());
        when(apartmentService.getById(any(UUID.class)))
                .thenReturn(apartmentEntity);

        assertThatThrownBy(() ->
                target.checkOut(clientId, apartmentId)
        ).isInstanceOf(ClientNotfoundException.class);
    }

    @Test
    void checkOut_NotExistingApartment_ShouldThrowApartmentNotfoundException() {
        final UUID clientId = UUID.randomUUID();
        final UUID apartmentId = UUID.randomUUID();
        when(repository.getById(any(UUID.class)))
                .thenReturn(Optional.of(client));
        when(apartmentService.getById(any(UUID.class)))
                .thenThrow(ApartmentNotfoundException.class);

        assertThatThrownBy(() ->
                target.checkOut(clientId, apartmentId)
        ).isInstanceOf(ApartmentNotfoundException.class);
    }

    @Test
    void checkOut_ValidEntities_ShouldRemoveApartmentFromClientSetAndUpdateApartmentStatus() {
        final UUID clientId = UUID.randomUUID();
        final UUID apartmentId = client.getApartments().stream().findFirst().get().id();
        when(repository.getById(any(UUID.class)))
                .thenReturn(Optional.of(client));
        when(apartmentService.getById(any(UUID.class)))
                .thenReturn(apartmentEntity);
        Apartment expected = new Apartment(apartmentEntity.id(), apartmentEntity.price(), apartmentEntity.capacity(),
                true, ApartmentStatus.AVAILABLE);
        when(repository.has(any(UUID.class)))
                .thenReturn(true);

        target.checkOut(clientId, apartmentId);

        verify(apartmentService).update(expected);
    }

    @Test
    void getById_ValidClient_ShouldReturnClientFromRepository() {
        final UUID id = UUID.randomUUID();
        when(repository.getById(any(UUID.class)))
                .thenReturn(Optional.of(client));

        ClientEntity actual = target.getById(id);

        assertThat(actual).isNotNull();
    }

    @Test
    void getById_NotExistingClient_ShouldThrowClientNotfoundException() {
        final UUID id = UUID.randomUUID();
        when(repository.getById(any(UUID.class)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                target.getById(id)
        ).isInstanceOf(ClientNotfoundException.class);
    }

    @Test
    void has_NotExistingClient_ShouldReturnFalse() {
        when(repository.has(any(UUID.class))).thenReturn(false);

        boolean actual = target.has(UUID.randomUUID());

        assertThat(actual).isFalse();
    }

    @Test
    void has_ExistingClient_ShouldReturnTrue() {
        when(repository.has(any(UUID.class))).thenReturn(true);

        boolean actual = target.has(UUID.randomUUID());

        assertThat(actual).isTrue();
    }

    @Test
    void save_ExistingClient_ShouldThrowClientIsAlreadyExistsException() {
        when(repository.has(any(UUID.class))).thenReturn(true);

        assertThatThrownBy(() ->
                target.save(client)
        ).isInstanceOf(ClientIsAlreadyExistsException.class);
    }

    @Test
    void save_NotExistingClient_ShouldCallRepositorySaveMethod() {
        when(repository.has(any(UUID.class))).thenReturn(false);

        target.save(client);

        verify(repository).save(any(Client.class));
    }

    @Test
    void delete_NotExistingClient_ShouldThrowClientNotfoundException() {
        final UUID id = UUID.randomUUID();
        when(repository.has(any(UUID.class))).thenReturn(false);

        assertThatThrownBy(() ->
                target.delete(id)
        ).isInstanceOf(ClientNotfoundException.class);
    }

    @Test
    void delete_ExistingClient_ShouldDeleteClientFromRepository() {
        when(repository.has(any(UUID.class))).thenReturn(true);

        target.delete(client.getId());

        verify(repository).delete(any(UUID.class));
    }

    @Test
    void update_NotExistingClient_ShouldThrowClientNotfoundException() {
        when(repository.has(any(UUID.class))).thenReturn(false);

        assertThatThrownBy(() ->
                target.update(client)
        ).isInstanceOf(ClientNotfoundException.class);
    }

    @Test
    void update_ExistingClient_ShouldUpdateClientInRepository() {
        when(repository.has(any(UUID.class))).thenReturn(true);

        target.update(client);

        verify(repository).update(client);
    }
}
