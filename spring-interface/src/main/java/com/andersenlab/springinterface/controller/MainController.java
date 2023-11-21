package com.andersenlab.springinterface.controller;

import com.andersenlab.hotel.usecase.ListClientsUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//TODO EXAMPLE
@RestController
@RequestMapping("/")
public class MainController {
    private final ListClientsUseCase listClientsUseCase;

    @Autowired
    public MainController(ListClientsUseCase listClientsUseCase) {
        this.listClientsUseCase = listClientsUseCase;
    }

    @GetMapping
    public String test() {
        return "hi, for ListClientsUseCase injected - " + listClientsUseCase.getClass().getName();
    }
}
