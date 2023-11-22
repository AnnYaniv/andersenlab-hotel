package com.andersenlab.springinterface.config;

import com.andersenlab.hotel.model.Apartment;
import com.andersenlab.hotel.model.ApartmentSort;
import com.andersenlab.hotel.model.Client;
import com.andersenlab.hotel.model.ClientSort;
import com.andersenlab.hotel.repository.SortableCrudRepository;
import com.andersenlab.hotel.service.impl.ApartmentService;
import com.andersenlab.hotel.service.impl.ClientService;
import com.andersenlab.springinterface.model.ClientCases;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class ServicesConfiguration {

    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
    public ApartmentService apartmentService(SortableCrudRepository<Apartment, ApartmentSort> apartmentRepository) {
        return new ApartmentService(apartmentRepository);
    }

    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
    public ClientService clientService(SortableCrudRepository<Client, ClientSort> clientRepository, ApartmentService apartmentService) {
        return new ClientService(clientRepository, apartmentService);
    }

    @Bean
    public ClientCases clientCases(ClientService clientService) {
        return new ClientCases(clientService, clientService,
                clientService, clientService, clientService);
    }
}
