package com.andersenlab.springinterface.controller;


import com.andersenlab.hotel.model.Apartment;
import com.andersenlab.hotel.model.ApartmentEntity;
import com.andersenlab.hotel.model.ApartmentSort;
import com.andersenlab.hotel.model.ApartmentStatus;
import com.andersenlab.hotel.service.impl.ApartmentService;
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
import java.math.BigInteger;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/apartments")
public class ApartmentsController {
    @Autowired
    ApartmentService apartmentService;

    @GetMapping("/")
    public List<ApartmentEntity> getAll(@RequestParam String sort) {
        return apartmentService.list(ApartmentSort.valueOf(sort.toUpperCase()));
    }

    @PostMapping("/")
    public ApartmentEntity create(@RequestBody BigDecimal price,
                       @RequestBody BigInteger capacity,
                       @RequestBody boolean availability,
                       @RequestBody ApartmentStatus status) {
        Apartment apartment = new Apartment(null, price, capacity, availability,status);
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
    public ApartmentEntity adjust(@RequestBody String clientId, @RequestBody BigDecimal newPrice) {
        apartmentService.adjust(UUID.fromString(clientId), newPrice);
        return apartmentService.getById(UUID.fromString(clientId));
    }

}
