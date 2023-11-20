package com.andersenlab.hotel.repository.jdbc;

import com.andersenlab.hotel.model.Client;
import com.andersenlab.hotel.model.ClientSort;
import com.andersenlab.hotel.model.ClientStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class JdbcClientRepositoryTest {

    private JdbcClientRepository target;
    private Client client1;
    private Client client2;

    AtomicInteger integer = new AtomicInteger(0);

    @BeforeEach
    void setUp() {
        JdbcConnector connector = new JdbcConnector("jdbc:h2:~/ht2-" + integer.incrementAndGet(), "sa", "")
                .migrate();

        target = new JdbcClientRepository(connector.getDatasourse());

        client1 = new Client(UUID.fromString("00000000-0000-0000-0000-000000000001"),
                "a", ClientStatus.NEW);
        client2 = new Client(UUID.fromString("00000000-0000-0000-0000-000000000002"),
                "b", ClientStatus.NEW);
    }

    @AfterEach
    void tearDown() {
        target.delete(client1.getId());
        target.delete(client2.getId());
    }

    @Test
    void save_SingleEntity_ShouldSaveEntityInStore() {
        target.save(client1);

        assertThat(target.has(client1.getId())).isTrue();
    }

    @Test
    void findAllSorted_EntityId_ShouldSortEntitiesFromStoreById() {
        ClientSort sort = ClientSort.ID;
        target.save(client2);
        target.save(client1);
        List<Client> expected = List.of(
                client1,
                client2
        );

        Collection<Client> actual = target.findAllSorted(sort);

        assertThat(actual).containsExactlyElementsOf(expected);
    }

    @Test
    void findAllSorted_EntityName_ShouldSortEntitiesFromStoreByPrice() {
        ClientSort sort = ClientSort.NAME;
        target.save(client2);
        target.save(client1);
        List<Client> expected = List.of(
                client1,
                client2
        );

        Collection<Client> actual = target.findAllSorted(sort);

        assertThat(actual).containsExactlyElementsOf(expected);
    }

    @Test
    void findAllSorted_EntityStatus_ShouldSortEntitiesFromStoreByCapacity() {
        ClientSort sort = ClientSort.STATUS;
        target.save(client1);
        client2.setStatus(ClientStatus.ADVANCED);
        target.save(client2);
        List<Client> expected = List.of(
                client2,
                client1
        );

        Collection<Client> actual =  target.findAllSorted(sort);

        assertThat(actual).containsExactlyElementsOf(expected);
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
