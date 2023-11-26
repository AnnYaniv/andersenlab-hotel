package com.andersenlab.springinterface.config;

import com.andersenlab.hotel.model.Apartment;
import com.andersenlab.hotel.model.ApartmentSort;
import com.andersenlab.hotel.model.Client;
import com.andersenlab.hotel.model.ClientSort;
import com.andersenlab.hotel.repository.SortableCrudRepository;
import com.andersenlab.hotel.service.impl.ApartmentService;
import com.andersenlab.hotel.service.impl.ClientService;
import com.andersenlab.hotel.usecase.CheckInClientUseCase;
import com.andersenlab.hotel.usecase.CheckOutClientUseCase;
import com.andersenlab.hotel.usecase.impl.BlockedCheckIn;
import com.andersenlab.hotel.usecase.impl.BlockedCheckOut;
import com.andersenlab.springinterface.model.ClientCases;
import org.springframework.beans.factory.annotation.Value;
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
    public ClientCases clientCases(ClientService clientService,
                                   @Value("${apartment.change.enabled}") boolean apartmentChangeEnabled) {
        CheckInClientUseCase checkInClientUseCase;
        CheckOutClientUseCase checkOutClientUseCase;

        if (apartmentChangeEnabled) {
            checkInClientUseCase = clientService;
            checkOutClientUseCase = clientService;
        } else {
            checkInClientUseCase = new BlockedCheckIn();
            checkOutClientUseCase = new BlockedCheckOut();
        }

        return new ClientCases(clientService, clientService, checkInClientUseCase, checkOutClientUseCase, clientService);
    }
}
