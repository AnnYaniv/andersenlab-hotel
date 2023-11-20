package com.andersenlab.hotel.repository.inmemory;

import com.andersenlab.hotel.model.Client;
import com.andersenlab.hotel.model.ClientStatus;
import com.andersenlab.hotel.model.ClientSort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryClientRepositoryTest {

    private InMemoryClientRepository target;
    private Client client1;
    private Client client2;
    private Client client3;
    private Client client4;
    private Client client5;

    @BeforeEach
    void setUp() {
        target = new InMemoryClientRepository();

        client1 = new Client(UUID.randomUUID(), "name-1", ClientStatus.NEW);

        client2 = new Client(UUID.randomUUID(), "name-2", ClientStatus.BANNED);

        client3 = new Client(UUID.fromString("9d76f43e-4ce2-40ba-90e1-37a6bc425ee1"),
                "name-3", ClientStatus.BANNED);

        client4 = new Client(UUID.fromString("9d76f43e-4ce2-40ba-90e1-37a6bc425ee2"),
                "name-4", ClientStatus.NEW);

        client5 = new Client(UUID.fromString("9d76f43e-4ce2-40ba-90e1-37a6bc425ee3"),
                "name-5", ClientStatus.ADVANCED);
    }

    @Test
    void save_SingleEntity_ShouldSaveEntityInStore() {
        target.save(client1);

        int actualSize = target.findAllSorted(ClientSort.ID).size();

        assertThat(actualSize).isEqualTo(1);
        assertThat(target.has(client1.getId())).isTrue();
    }

    @Test
    void save_MultipleEntity_ShouldSaveEntityInStore() {
        target.save(client2);
        target.save(client3);

        int actualSize = target.findAllSorted(ClientSort.ID).size();

        assertThat(actualSize).isEqualTo(2);
        assertThat(target.has(client2.getId())).isTrue();
        assertThat(target.has(client3.getId())).isTrue();
    }

    @Test
    void delete_ExistingEntity_ShouldDeleteEntityFromStore() {
        target.save(client1);

        target.delete(client1.getId());

        assertThat(target.has(client2.getId())).isFalse();
    }

    @Test
    void delete_NonExistingEntity_ShouldDeleteEntityFromStore() {
        target.delete(client1.getId());

        assertThat(target.has(client1.getId())).isFalse();
    }

    @Test
    void getById_ExistingEntity_ShouldReturnEntityFromStore() {
        target.save(client1);

        Optional<Client> actual = target.getById(client1.getId());

        assertThat(actual).isPresent().containsSame(client1);
    }

    @Test
    void getById_NonExistingEntity_ShouldReturnNullEntity() {
        Optional<Client> actual = target.getById(client1.getId());

        assertThat(actual).isNotPresent();
    }

    @Test
    void findAllSorted_EntityId_ShouldSortEntitiesFromStoreById() {
        target.save(client5);
        target.save(client4);
        target.save(client3);

        List<Client> sorted = (List<Client>)
                target.findAllSorted(ClientSort.ID);

        assertThat(client3).isEqualTo(sorted.get(0));
        assertThat(client4).isEqualTo(sorted.get(1));
        assertThat(client5).isEqualTo(sorted.get(2));
    }

    @Test
    void findAllSorted_EntityName_ShouldSortEntitiesFromStoreByName() {
        target.save(client5);
        target.save(client4);
        target.save(client3);

        List<Client> sorted = (List<Client>)
                target.findAllSorted(ClientSort.NAME);

        assertThat(client3).isEqualTo(sorted.get(0));
        assertThat(client4).isEqualTo(sorted.get(1));
        assertThat(client5).isEqualTo(sorted.get(2));
    }

    @Test
    void findAllSorted_EntityStatus_ShouldSortEntitiesFromStoreByStatus() {
        target.save(client3);
        target.save(client4);
        target.save(client5);

        List<Client> expected = Stream.of(client3, client4, client5)
                .sorted(ClientSort.STATUS.getComparator())
                .toList();

        List<Client> sorted = (List<Client>)
                target.findAllSorted(ClientSort.STATUS);

        assertThat(sorted).isEqualTo(expected);
    }

    @Test
    void has_ExistingEntity_ShouldReturnTrue() {
        target.save(client1);

        assertThat(target.has(client1.getId())).isTrue();
    }

    @Test
    void has_NonExistingEntity_ShouldReturnFalse() {
        assertThat(target.has(client1.getId())).isFalse();
    }
}
