package com.andersenlab.hotel.repository.jpa;

import com.andersenlab.hotel.model.Apartment;
import com.andersenlab.hotel.model.ApartmentSort;
import com.andersenlab.hotel.model.ApartmentStatus;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class JpaApartmentRepositoryTest {

    EntityManagerFactory factory;
    JpaApartmentRepository repository;
    Apartment apartment1;
    Apartment apartment2;
    Apartment apartment3;

    @BeforeEach
    @SneakyThrows
    void setUp() {
        factory = Persistence.createEntityManagerFactory("test_apartment");
        repository = new JpaApartmentRepository(factory);

        apartment1 = new Apartment(UUID.fromString("00000000-0000-0000-0000-000000000001"),
                BigDecimal.valueOf(1.0), BigInteger.ONE, true, ApartmentStatus.AVAILABLE);

        apartment2 = new Apartment(UUID.fromString("00000000-0000-0000-0000-000000000002"),
                BigDecimal.valueOf(2.0), BigInteger.TWO, false, ApartmentStatus.RESERVED);

        apartment3 = new Apartment(UUID.fromString("00000000-0000-0000-0000-000000000003"),
                BigDecimal.valueOf(10.0), BigInteger.TEN, true, ApartmentStatus.AVAILABLE);
    }

    @Test
    void save_SingleApartment_ShouldSaveEntity() {
        repository.save(apartment1);
        Optional<Apartment> actual = repository.getById(apartment1.getId());

        assertThat(actual).contains(apartment1);
    }

    @Test
    void findAllSorted_EntityId_ShouldSortEntitiesById() {
        ApartmentSort sort = ApartmentSort.ID;

        repository.save(apartment2);
        repository.save(apartment1);

        List<Apartment> actual = (List<Apartment>) repository.findAllSorted(sort);
        List<Apartment> expected = Stream.of(apartment1, apartment2)
                .sorted(sort.getComparator()).toList();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void findAllSorted_EntityPrice_ShouldSortEntitiesFromDbByPrice() {
        ApartmentSort sort = ApartmentSort.PRICE;
        repository.save(apartment1);
        repository.save(apartment3);
        repository.save(apartment2);

        List<Apartment> actual = (List<Apartment>) repository.findAllSorted(sort);

        List<Apartment> expected = Stream.of(apartment1, apartment2, apartment3)
                .sorted(sort.getComparator()).toList();

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void findAllSorted_EntityCapacity_ShouldSortEntitiesFromDbByCapacity() {
        ApartmentSort sort = ApartmentSort.CAPACITY;
        repository.save(apartment1);
        repository.save(apartment3);
        repository.save(apartment2);

        List<Apartment> actual = (List<Apartment>) repository.findAllSorted(sort);

        List<Apartment> expected = Stream.of(apartment1, apartment2, apartment3)
                .sorted(sort.getComparator()).toList();

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void findAllSorted_EntityAvailability_ShouldSortEntitiesFromDbByAvailability() {
        ApartmentSort sort = ApartmentSort.AVAILABILITY;
        repository.save(apartment1);
        repository.save(apartment3);
        repository.save(apartment2);

        List<Apartment> actual = (List<Apartment>) repository.findAllSorted(sort);

        List<Apartment> expected = Stream.of(apartment1, apartment2, apartment3)
                .sorted(sort.getComparator()).toList();

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void delete_ExistingEntity_ShouldDeleteEntityFromDb() {
        repository.save(apartment1);

        repository.delete(apartment1.getId());

        assertThat(repository.has(apartment1.getId())).isFalse();
    }

    @Test
    void has_ExistingEntity_ShouldReturnTrue() {
        repository.save(apartment1);

        assertThat(repository.has(apartment1.getId())).isTrue();
    }

    @Test
    void has_NonExistingEntity_ShouldReturnFalse() {
        assertThat(repository.has(apartment1.getId())).isFalse();
    }

    @Test
    void getById_ExistingEntity_ShouldReturnEntityFromDb() {
        repository.save(apartment1);

        assertThat(repository.getById(apartment1.getId())).contains(apartment1);
    }

    @Test
    void getById_ExistingEntity_ShouldReturnEmptyOptional() {
        Optional<Apartment> actual = repository.getById(apartment1.getId());

        assertThat(actual).isEmpty();
    }

    @Test
    void update_ExistingEntity_ShouldUpdateEntityInDb() {
        repository.save(apartment1);
        apartment2.setId(apartment1.getId());

        repository.update(apartment2);

        assertThat(repository.getById(apartment1.getId())).contains(apartment2);
    }
}
