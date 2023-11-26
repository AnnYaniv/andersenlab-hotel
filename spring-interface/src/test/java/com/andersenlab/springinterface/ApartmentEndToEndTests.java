package com.andersenlab.springinterface;


import com.andersenlab.hotel.model.Apartment;
import com.andersenlab.hotel.model.ApartmentEntity;
import com.andersenlab.hotel.model.ApartmentSort;
import com.andersenlab.hotel.model.ApartmentStatus;
import com.andersenlab.hotel.repository.SortableCrudRepository;

import com.andersenlab.springinterface.dto.ApartmentDto;
import com.andersenlab.springinterface.model.ErrorResponse;
import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApartmentEndToEndTests {
    @LocalServerPort
    private int port;

    @Autowired
    private SortableCrudRepository<Apartment, ApartmentSort> apartmentRepository;

    @Autowired
    private TestRestTemplate restTemplate;
    private String uri;
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
        Stream.of(apartment1, apartment2)
                .filter(apartment -> apartmentRepository.has(apartment.getId()))
                .forEach(apartment -> apartmentRepository.delete(apartment.getId()));
    }

    @Test
    @SneakyThrows
    void getApartmentByExistingId_shouldReturnApartmentEntity() {
        apartmentRepository.save(apartment1);

        ApartmentEntity actual = restTemplate.getForObject(uri + "/" + apartment1.getId().toString(),
                ApartmentEntity.class);

        assertThat(actual).isEqualTo(apartmentEntity1);
    }

    @Test
    void getApartmentByNonExistingId_shouldRespondBadRequest() {
        ResponseEntity<ErrorResponse> actual = restTemplate.exchange(uri + "/" + apartment1.getId(),
                HttpMethod.GET, null, ErrorResponse.class);

        assertThat(actual.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(400));
    }

    @Test
    void findAllSorted_ApartmentId_ShouldReturnStoredByIdListApartmentEntity() {
        apartmentRepository.save(apartment1);
        apartmentRepository.save(apartment2);

        ResponseEntity<List<ApartmentEntity>> actual = restTemplate.exchange(uri + "?sort=id",
                HttpMethod.GET, null, new ParameterizedTypeReference<>() {
                });

        assertThat(List.of(apartmentEntity1, apartmentEntity2)).isEqualTo(actual.getBody());
    }

    @Test
    void findAllSorted_ApartmentPrice_ShouldReturnStoredByIdListApartmentEntity() {
        apartmentRepository.save(apartment1);
        apartmentRepository.save(apartment2);

        ResponseEntity<List<ApartmentEntity>> actual = restTemplate.exchange(uri + "?sort=price",
                HttpMethod.GET, null, new ParameterizedTypeReference<>() {
                });

        assertThat(List.of(apartmentEntity2, apartmentEntity1)).isEqualTo(actual.getBody());
    }

    @Test
    void findAllSorted_ApartmentAvailability_ShouldReturnStoredByIdListApartmentEntity() {
        apartmentRepository.save(apartment1);
        apartmentRepository.save(apartment2);


        ResponseEntity<List<ApartmentEntity>> actual = restTemplate.exchange(uri + "?sort=availability",
                HttpMethod.GET, null, new ParameterizedTypeReference<>() {
                });

        assertThat(List.of(apartmentEntity1, apartmentEntity2)).isEqualTo(actual.getBody());
    }

    @Test
    void findAllSorted_ApartmentCapacity_ShouldReturnStoredByIdListApartmentEntity() {
        apartmentRepository.save(apartment1);
        apartmentRepository.save(apartment2);

        ResponseEntity<List<ApartmentEntity>> actual = restTemplate.exchange(uri + "?sort=capacity",
                HttpMethod.GET, null, new ParameterizedTypeReference<>() {
                });

        assertThat(List.of(apartmentEntity2, apartmentEntity1)).isEqualTo(actual.getBody());
    }


    @Test
    void deleteApartmentWithExistingId_shouldReturnStatusCode200() {
        apartmentRepository.save(apartment1);

        ResponseEntity<Object> actual = restTemplate.exchange(uri + "/{id}", HttpMethod.DELETE, null,
                Object.class, apartment1.getId());

        assertThat(actual.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
    }

    @Test
    void deleteApartmentNonWithExistingId_shouldReturnBadRequest() {
        ResponseEntity<ErrorResponse> actual = restTemplate.exchange(uri + "/{id}", HttpMethod.DELETE, null,
                ErrorResponse.class, apartment1.getId());

        assertThat(actual.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(400));
    }

    @Test
    void addApartment_shouldAddApartment() {
        ApartmentDto dto = new ApartmentDto(
                apartment1.getId(), apartment1.getPrice(), apartment1.getCapacity(),
                apartment1.isAvailability(), apartment1.getStatus()
        );

        ApartmentEntity actual = restTemplate.postForObject(uri, dto, ApartmentEntity.class);

        assertThat(actual).isEqualTo(apartmentEntity1);
    }

    @Test
    void addExistingApartment_shouldReturnBadRequest() {
        apartmentRepository.save(apartment1);

        ApartmentDto dto = new ApartmentDto(
                apartment1.getId(), apartment1.getPrice(), apartment1.getCapacity(),
                apartment1.isAvailability(), apartment1.getStatus()
        );

        ResponseEntity<ErrorResponse> actual = restTemplate.postForEntity(uri, dto, ErrorResponse.class);

        assertThat(actual.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(400));
    }

    @Test
    void adjustApartmentPrice_shouldReturnApartmentEntityWithNewPrice() {
        BigDecimal newPrice = BigDecimal.valueOf(666L);
        apartmentRepository.save(apartment1);

        ApartmentEntity expected = new ApartmentEntity(apartment1.getId(), newPrice, apartment1.getCapacity(),
                apartment1.isAvailability(), apartment1.getStatus());

        ResponseEntity<ApartmentEntity> actual = restTemplate.exchange(
                RequestEntity.put(uri + "/adjust")
                        .body(
                                Map.of(
                                        "clientId", apartment1.getId().toString(),
                                        "newPrice", newPrice
                                )
                        ), ApartmentEntity.class);

        assertThat(actual.getBody()).isEqualTo(expected);
    }

    @Test
    void adjustApartmentPriceIfIdNotExist_shouldReturnBadRequest() {
        BigDecimal newPrice = BigDecimal.valueOf(666L);

        ResponseEntity<ErrorResponse> actual = restTemplate.exchange(
                RequestEntity.put(uri + "/adjust")
                        .body(
                                Map.of(
                                        "clientId", apartment1.getId().toString(),
                                        "newPrice", newPrice
                                )
                        ), ErrorResponse.class);

        assertThat(actual.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(400));
    }
}
