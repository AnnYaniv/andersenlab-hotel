package com.andersenlab.hotel.repository.jpa;

import com.andersenlab.hotel.model.Apartment;
import com.andersenlab.hotel.model.ApartmentEntity;
import com.andersenlab.hotel.model.Client;
import com.andersenlab.hotel.model.ClientSort;
import com.andersenlab.hotel.repository.SortableCrudRepository;
import com.andersenlab.hotel.usecase.exception.ApartmentNotfoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Root;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class JpaClientRepository implements SortableCrudRepository<Client, ClientSort> {
    private final EntityManager manager;
    private final Mapper mapper;

    public JpaClientRepository(final EntityManagerFactory managerFactory) {
        this.manager = managerFactory.createEntityManager();
        this.mapper = new Mapper(manager);
    }

    @Override
    public void save(Client entity) {
        EntityTransaction transaction = manager.getTransaction();
        transaction.begin();
        manager.persist(mapper.toClientJpa(entity));
        transaction.commit();
    }

    @Override
    public Collection<Client> findAllSorted(ClientSort sort) {
        CriteriaBuilder criteriaBuilder = manager.getCriteriaBuilder();
        CriteriaQuery<ClientJpa> criteriaQuery = criteriaBuilder.createQuery(ClientJpa.class);
        Root<ClientJpa> rootEntry = criteriaQuery.from(ClientJpa.class);
        List<Order> orderList = new ArrayList<>();
        orderList.add(criteriaBuilder.asc(rootEntry.get(sort.name().toLowerCase())));

        CriteriaQuery<ClientJpa> all = criteriaQuery.select(rootEntry);
        CriteriaQuery<ClientJpa> orderedCriteria = all.orderBy(orderList);

        TypedQuery<ClientJpa> allQuery = manager.createQuery(orderedCriteria);
        return allQuery.getResultStream().map(mapper::toClient).toList();
    }

    @Override
    public void delete(UUID id) {
        EntityTransaction transaction = manager.getTransaction();
        transaction.begin();
        manager.remove(manager.find(ClientJpa.class, id));
        transaction.commit();
    }

    @Override
    public boolean has(UUID id) {
        return getById(id).isPresent();
    }

    @Override
    public Optional<Client> getById(UUID id) {
        ClientJpa clientJpa = manager.find(ClientJpa.class, id);
        if (clientJpa != null) {
            return Optional.of(mapper.toClient(clientJpa));
        }
        return Optional.empty();
    }

    @Override
    public void update(Client entity) {
        EntityTransaction transaction = manager.getTransaction();
        transaction.begin();
        manager.merge(mapper.toClientJpa(entity));
        transaction.commit();
    }

    static class Mapper {
        private final EntityManager manager;
        Mapper(EntityManager manager) {
            this.manager = manager;
        }
        public ClientJpa toClientJpa(Client client) {
            return new ClientJpa(
                    client.getId(), client.getName(), client.getStatus(),
                    client.getApartments().stream()
                            .map(entity -> getApartment(entity.id()))
                            .collect(Collectors.toSet())
            );
        }

        private Apartment getApartment(UUID id) {
            Apartment apartment = manager.find(Apartment.class, id);
            if (apartment == null) {
                throw new ApartmentNotfoundException();
            }
            return apartment;
        }

        public Client toClient(ClientJpa clientJpa) {
            return new Client(
                    clientJpa.getId(), clientJpa.getName(), clientJpa.getStatus(),
                    clientJpa.getApartments().stream()
                            .map(apartment -> new ApartmentEntity(
                                    apartment.getId(), apartment.getPrice(), apartment.getCapacity(),
                                    apartment.isAvailability(), apartment.getStatus())
                            ).collect(Collectors.toSet())
            );
        }
    }
}
