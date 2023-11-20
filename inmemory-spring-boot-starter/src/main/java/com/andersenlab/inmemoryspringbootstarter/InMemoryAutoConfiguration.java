package com.andersenlab.inmemoryspringbootstarter;

import com.andersenlab.hotel.model.Apartment;
import com.andersenlab.hotel.model.ApartmentSort;
import com.andersenlab.hotel.model.Client;
import com.andersenlab.hotel.model.ClientSort;
import com.andersenlab.hotel.repository.SortableCrudRepository;
import com.andersenlab.hotel.repository.inmemory.InMemoryApartmentRepository;
import com.andersenlab.hotel.repository.inmemory.InMemoryClientRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InMemoryAutoConfiguration {
    @Bean
    public SortableCrudRepository<Client, ClientSort> clientRepository() {
        return new InMemoryClientRepository();
    }

    @Bean
    public SortableCrudRepository<Apartment, ApartmentSort> apartmentRepository() {
        return new InMemoryApartmentRepository();
    }
}
