package com.andersenlab.springinterface;


import com.andersenlab.hotel.model.Apartment;
import com.andersenlab.hotel.model.ApartmentEntity;
import com.andersenlab.hotel.model.ApartmentSort;
import com.andersenlab.hotel.model.ApartmentStatus;
import com.andersenlab.hotel.repository.SortableCrudRepository;
import com.andersenlab.hotel.service.impl.ApartmentService;

import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ApartmentEndToEndTests {
    @LocalServerPort
    private int port;
    @Autowired
    private ApartmentService apartmentService;
    @Autowired
    private SortableCrudRepository<Apartment, ApartmentSort> apartmentRepository;
    @Autowired
    private TestRestTemplate restTemplate;
    private  String uri;
    Apartment apartment1;
    Apartment apartment2;
    ApartmentEntity apartmentEntity1;
    ApartmentEntity apartmentEntity2;



    @BeforeEach
    void setUp() {
        uri = "http://localhost:" + port + "/apartments";
        apartment1 = new Apartment(UUID.fromString("00000000-0000-0000-0000-000000000003"),
                BigDecimal.TEN, BigInteger.TEN, true, ApartmentStatus.AVAILABLE);
        apartment2 = new Apartment(UUID.fromString("00000000-0000-0000-0000-000000000004"),
                BigDecimal.ONE, BigInteger.TWO, true, ApartmentStatus.AVAILABLE);
        apartmentEntity1 = new ApartmentEntity(apartment1.getId(), apartment1.getPrice(), apartment1.getCapacity(),
                apartment1.isAvailability(), apartment1.getStatus());
        apartmentEntity2 = new ApartmentEntity(apartment2.getId(), apartment2.getPrice(), apartment2.getCapacity(),
                apartment2.isAvailability(), apartment2.getStatus());
    }

    @AfterEach
    void teardown() {
        apartmentRepository.delete(apartment1.getId());
        apartmentRepository.delete(apartment2.getId());
    }

    @Test
    @SneakyThrows
    void getApartmentByExistingId_shouldReturnApartmentEntity() {
        apartmentRepository.save(apartment1);

        ApartmentEntity actual = restTemplate.getForObject(uri + "/" + apartment1.getId().toString(),
                ApartmentEntity.class);

        assertThat(actual.id().toString()).isEqualTo(apartment1.getId().toString());
    }
    @Test
    @Disabled
    @SneakyThrows
    void getApartmentByNonExistingId_shouldRespondBadRequest() {//shouldRespondBadRequest
        ResponseEntity<ApartmentEntity> actual = restTemplate.exchange(uri + "/" + apartment1.getId(),
                HttpMethod.GET, null, new ParameterizedTypeReference<>() {
                });

                assertThat(actual.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(400));
    }

    @Test
    @SneakyThrows
    void findAllSorted_ApartmentId_ShouldReturnStoredByIdListApartmentEntity() {
        apartmentRepository.save(apartment1);
        apartmentRepository.save(apartment2);

        ResponseEntity<List<ApartmentEntity>> actual = restTemplate.exchange(uri + "?sort=id",
                HttpMethod.GET, null, new ParameterizedTypeReference<>() {
                });

        assertThat(List.of(apartmentEntity1,apartmentEntity2)).isEqualTo(actual.getBody());
    }


}
