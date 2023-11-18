package com.andersenlab.hotel.repository.jdbc;

import com.andersenlab.hotel.model.Apartment;
import com.andersenlab.hotel.model.ApartmentSort;
import com.andersenlab.hotel.model.ApartmentStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class JdbcApartmentRepositoryTest {

    JdbcApartmentRepository target;
    Apartment apartment1;
    Apartment apartment2;
    Apartment apartment3;
    Apartment apartment4;
    Apartment apartment5;
    AtomicInteger integer = new AtomicInteger(0);

    @BeforeEach
    void setUp() {
        JdbcConnector connector = new JdbcConnector("jdbc:h2:~/ht1-" + integer.incrementAndGet(), "sa", "")
                .migrate();

        target = new JdbcApartmentRepository(connector.getDatasourse());

        apartment1 = new Apartment(UUID.fromString("00000000-0000-0000-0000-000000000001"),
                BigDecimal.valueOf(1.0), BigInteger.ONE, true, ApartmentStatus.AVAILABLE);

        apartment2 = new Apartment(UUID.fromString("00000000-0000-0000-0000-000000000002"),
                BigDecimal.valueOf(2.0), BigInteger.TWO, false, ApartmentStatus.RESERVED);

        apartment3 = new Apartment(UUID.fromString("00000000-0000-0000-0000-000000000003"),
                BigDecimal.valueOf(10.0), BigInteger.TEN, true, ApartmentStatus.AVAILABLE);

        apartment4 = new Apartment(UUID.fromString("00000000-0000-0000-0000-000000000004"),
                BigDecimal.valueOf(2.0), BigInteger.TWO, true, ApartmentStatus.AVAILABLE);

        apartment5 = new Apartment(UUID.fromString("00000000-0000-0000-0000-000000000005"),
                BigDecimal.valueOf(1.0), BigInteger.ONE, false, ApartmentStatus.AVAILABLE);
    }

    @AfterEach
    void tearDown() {
        target.delete(apartment1.getId());
        target.delete(apartment2.getId());
        target.delete(apartment3.getId());
        target.delete(apartment4.getId());
        target.delete(apartment5.getId());
    }

    @Test
    void save_SingleEntity_ShouldSaveEntityInStore() {
        target.save(apartment1);

        assertThat(target.has(apartment1.getId())).isTrue();
    }

    @Test
    void findAllSorted_EntityId_ShouldSortEntitiesFromStoreById() {
        ApartmentSort sort = ApartmentSort.ID;
        target.save(apartment1);
        target.save(apartment3);
        target.save(apartment2);

        List<Apartment> actual = (List<Apartment>) target.findAllSorted(sort);

        List<Apartment> expected = Stream.of(apartment1, apartment2, apartment3)
                .sorted(sort.getComparator()).toList();

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void findAllSorted_EntityPrice_ShouldSortEntitiesFromStoreByPrice() {
        ApartmentSort sort = ApartmentSort.PRICE;
        target.save(apartment1);
        target.save(apartment3);
        target.save(apartment2);

        List<Apartment> actual = (List<Apartment>) target.findAllSorted(sort);

        List<Apartment> expected = Stream.of(apartment1, apartment2, apartment3)
                .sorted(sort.getComparator()).toList();

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void findAllSorted_EntityCapacity_ShouldSortEntitiesFromStoreByCapacity() {
        ApartmentSort sort = ApartmentSort.CAPACITY;
        target.save(apartment1);
        target.save(apartment3);
        target.save(apartment2);

        List<Apartment> actual = (List<Apartment>) target.findAllSorted(sort);

        List<Apartment> expected = Stream.of(apartment1, apartment2, apartment3)
                .sorted(sort.getComparator()).toList();

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void findAllSorted_EntityAvailability_ShouldSortEntitiesFromStoreByAvailability() {
        ApartmentSort sort = ApartmentSort.AVAILABILITY;
        target.save(apartment1);
        target.save(apartment3);
        target.save(apartment2);

        List<Apartment> actual = (List<Apartment>) target.findAllSorted(sort);

        List<Apartment> expected = Stream.of(apartment1, apartment2, apartment3)
                .sorted(sort.getComparator()).toList();

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void delete_ExistingEntity_ShouldDeleteEntityFromStore() {
        target.save(apartment1);

        target.delete(apartment1.getId());

        assertThat(target.has(apartment1.getId())).isFalse();
    }

    @Test
    void has_ExistingEntity_ShouldReturnTrue() {
        target.save(apartment1);

        assertThat(target.has(apartment1.getId())).isTrue();
    }

    @Test
    void has_NonExistingEntity_ShouldReturnFalse() {
        assertThat(target.has(apartment1.getId())).isFalse();
    }

    @Test
    void getById_ExistingEntity_ShouldReturnEntityFromStore() {
        target.save(apartment1);

        assertThat(target.getById(apartment1.getId())).contains(apartment1);
    }

    @Test
    void getById_ExistingEntity_ShouldReturnEmptyOptional() {
        Optional<Apartment> actual = target.getById(apartment1.getId());

        assertThat(actual).isEmpty();
    }

    @Test
    void update_ExistingEntity_ShouldUpdateEntityInStore() {
        target.save(apartment1);
        apartment2.setId(apartment1.getId());

        target.update(apartment2);

        assertThat(target.getById(apartment1.getId())).contains(apartment2);
    }
}