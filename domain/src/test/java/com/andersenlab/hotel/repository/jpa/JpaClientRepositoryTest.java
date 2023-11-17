package com.andersenlab.hotel.repository.jpa;

import com.andersenlab.hotel.model.Apartment;
import com.andersenlab.hotel.model.ApartmentEntity;
import com.andersenlab.hotel.model.ApartmentStatus;
import com.andersenlab.hotel.model.Client;
import com.andersenlab.hotel.model.ClientSort;
import com.andersenlab.hotel.model.ClientStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class JpaClientRepositoryTest {

    private static EntityManagerFactory factory;
    private EntityManager manager;
    private JpaClientRepository target;

    private Client client1;
    private Client client2;
    private Client client3;

    private Apartment apartment1;
    private Apartment apartment2;

    private ApartmentEntity apartmentEntity1;
    private ApartmentEntity apartmentEntity2;

    @BeforeAll
    static void beforeAll() {
        factory = Persistence.createEntityManagerFactory("test_persistence");
    }

    @BeforeEach
    @SneakyThrows
    void setUp() {
        manager = factory.createEntityManager();
        target = new JpaClientRepository(factory);

        setUpEntities();
        saveApartmentsInContext();
    }

    void setUpEntities() {
        apartment1 = new Apartment(UUID.fromString("00000000-0000-0000-0000-000000000001"),
                BigDecimal.ONE, BigInteger.ONE, false, ApartmentStatus.RESERVED);

        apartment2 = new Apartment(UUID.fromString("00000000-0000-0000-0000-000000000002"),
                BigDecimal.TWO, BigInteger.TWO, true, ApartmentStatus.AVAILABLE);

        apartmentEntity1 = new ApartmentEntity(apartment1.getId(), apartment1.getPrice(),
                apartment1.getCapacity(), apartment1.isAvailability(), apartment1.getStatus());

        apartmentEntity2 = new ApartmentEntity(apartment2.getId(), apartment2.getPrice(),
                apartment2.getCapacity(), apartment2.isAvailability(), apartment2.getStatus());

        client1 = new Client(UUID.fromString("00000000-0000-0000-0000-000000000001"),
                "name-1", ClientStatus.NEW);

        client2 = new Client(UUID.fromString("00000000-0000-0000-0000-000000000002"),
                "name-2", ClientStatus.BANNED);

        client3 = new Client(UUID.fromString("00000000-0000-0000-0000-000000000003"),
                "name-3", ClientStatus.ADVANCED, Set.of(apartmentEntity1));
    }

    void saveApartmentsInContext() { //TODO Change to actual ApartmentRepositoryJpa
        EntityTransaction transaction = manager.getTransaction();
        transaction.begin();
        manager.persist(apartment1);
        manager.persist(apartment2);
        manager.flush();
        transaction.commit();
    }

    @Test
    void save_NotExistingEntity_ShouldSaveClient() {
        target.save(client1);

        assertThat(target.has(client1.getId())).isTrue();
    }

    @Test
    void save_WithMultipleApartments_ShouldSaveClient() {
        target.save(client3);

        Optional<Client> actual = target.getById(client3.getId());

        assertThat(actual).isPresent();
        assertThat(actual.get().getApartments()).hasSameSizeAs(client3.getApartments());
    }

    @Test
    void findAllSorted_EntityId_ShouldSortEntitiesFromStoreById() {
        ClientSort sort = ClientSort.ID;

        target.save(client1);
        target.save(client2);
        target.save(client3);

        List<Client> expected = Stream.of(client1, client2, client3).sorted(sort.getComparator()).toList();
        List<Client> actual = (List<Client>) target.findAllSorted(sort);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void findAllSorted_EntityStatus_ShouldSortEntitiesFromStoreById() {
        ClientSort sort = ClientSort.STATUS;

        target.save(client1);
        target.save(client2);
        target.save(client3);

        List<Client> expected = Stream.of(client1, client2, client3)
                .sorted(sort.getComparator()).toList();

        List<Client> actual = (List<Client>) target.findAllSorted(sort);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void findAllSorted_EntityName_ShouldSortEntitiesFromStoreById() {
        ClientSort sort = ClientSort.NAME;

        target.save(client1);
        target.save(client2);
        target.save(client3);

        target.getById(client1.getId()).ifPresent(System.out::println);

        List<Client> expected = Stream.of(client1, client2, client3)
                .sorted(sort.getComparator()).toList();

        List<Client> actual = (List<Client>) target.findAllSorted(sort);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void delete_ExistingEntity_ShouldDeleteEntityFromStore() {
        target.save(client1);

        target.delete(client1.getId());

        assertThat(target.has(client1.getId())).isFalse();
    }

    @Test
    void has_ExistingEntity_ShouldReturnTrue() {
        target.save(client1);

        assertThat(target.has(client1.getId())).isTrue();
    }

    @Test
    void has_NonExistingEntity_ShouldReturnFalse() {
        assertThat(target.has(client2.getId())).isFalse();
    }

    @Test
    void getById_ExistingEntity_ShouldReturnEntityFromStore() {
        target.save(client1);

        assertThat(target.getById(client1.getId())).contains(client1);
    }

    @Test
    void getById_ExistingEntity_ShouldReturnEmptyOptional() {
        Optional<Client> actual = target.getById(client1.getId());

        assertThat(actual).isEmpty();
    }

    @Test
    void update_ExistingEntity_ShouldUpdateEntityInStore() {
        target.save(client1);
        client2.setId(client1.getId());

        target.update(client2);

        assertThat(target.getById(client1.getId())).contains(client2);
    }
}