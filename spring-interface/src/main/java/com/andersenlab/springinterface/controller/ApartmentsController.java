package com.andersenlab.springinterface.controller;


import com.andersenlab.hotel.model.*;
import com.andersenlab.hotel.service.impl.ApartmentService;
import com.andersenlab.springinterface.dto.ApartmentDto;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/apartments")
public class ApartmentsController {
    private final ApartmentService apartmentService;

    @Autowired
    public ApartmentsController(ApartmentService apartmentService) {
        this.apartmentService = apartmentService;
    }

    @GetMapping
    public List<ApartmentEntity> getAll(@RequestParam String sort) {
        ApartmentSort clientSort = EnumUtils.getEnum(ApartmentSort.class, sort.toUpperCase());
        return apartmentService.list(clientSort);
    }

    @PostMapping
    public ApartmentEntity create(@RequestBody ApartmentDto apartmentDto) {
        Apartment apartment = apartmentDto.getApartment();
        apartmentService.save(apartment);
        return apartmentService.getById(apartment.getId());
    }

    @GetMapping("/{id}")
    public ApartmentEntity getById(@PathVariable String id) {
        return apartmentService.getById(UUID.fromString(id));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        apartmentService.delete(UUID.fromString(id));
    }

    @PutMapping("/adjust")
    public ApartmentEntity adjust(@RequestBody AdjustRequest request) {
        apartmentService.adjust(UUID.fromString(request.clientId), request.newPrice);
        return apartmentService.getById(UUID.fromString(request.clientId));
    }

    record AdjustRequest(String clientId, BigDecimal newPrice) {
    }
}
