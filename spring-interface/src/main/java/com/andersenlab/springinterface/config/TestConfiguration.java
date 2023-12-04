package com.andersenlab.springinterface.config;

import com.andersenlab.hotel.service.impl.ClientService;
import com.andersenlab.springinterface.model.ClientCases;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("test")
@Configuration
public class TestConfiguration {

    @Bean
    public ClientCases clientCases(ClientService clientService) {

        return new ClientCases(clientService, clientService, clientService, clientService, clientService);
    }
}
