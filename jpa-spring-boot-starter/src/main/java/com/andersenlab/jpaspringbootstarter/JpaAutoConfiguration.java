package com.andersenlab.jpaspringbootstarter;

import com.andersenlab.hotel.model.Apartment;
import com.andersenlab.hotel.model.ApartmentSort;
import com.andersenlab.hotel.model.Client;
import com.andersenlab.hotel.model.ClientSort;
import com.andersenlab.hotel.repository.SortableCrudRepository;
import com.andersenlab.hotel.repository.jpa.JpaApartmentRepository;
import com.andersenlab.hotel.repository.jpa.JpaClientRepository;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;

import java.util.Map;

@AutoConfiguration
public class JpaAutoConfiguration {
    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
    @Primary
    public SortableCrudRepository<Apartment, ApartmentSort> apartmentRepository(EntityManagerFactory entityManagerFactory) {
        return new JpaApartmentRepository(entityManagerFactory);
    }

    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
    @Primary
    public SortableCrudRepository<Client, ClientSort> clientRepository(EntityManagerFactory entityManagerFactory) {
        return new JpaClientRepository(entityManagerFactory);
    }

    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
    public EntityManagerFactory entityManagerFactory(@Value("${persistence.jdbc.url}") String url) {
        return Persistence.createEntityManagerFactory("postgres",
                Map.of(
                        "jakarta.persistence.jdbc.url", url
                ));
    }
}
