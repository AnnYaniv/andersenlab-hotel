package com.andersenlab.springinterface;

import com.andersenlab.hotel.model.Apartment;
import com.andersenlab.hotel.model.ApartmentEntity;
import com.andersenlab.hotel.model.ApartmentSort;
import com.andersenlab.hotel.model.ApartmentStatus;
import com.andersenlab.hotel.model.Client;
import com.andersenlab.hotel.model.ClientEntity;
import com.andersenlab.hotel.model.ClientSort;
import com.andersenlab.hotel.model.ClientStatus;
import com.andersenlab.hotel.repository.SortableCrudRepository;
import com.andersenlab.hotel.service.impl.ClientService;
import com.andersenlab.springinterface.dto.ClientDto;
import com.andersenlab.springinterface.model.ErrorResponse;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.MethodName.class)
@ActiveProfiles("test")
class ClientEndToEndTests {
    @Container
    public static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer("postgres:11.1")
            .withDatabaseName("hotel-jpa")
            .withUsername("postgres")
            .withPassword("root");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("persistence.jdbc.url", postgreSQLContainer::getJdbcUrl);
    }


    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private SortableCrudRepository<Client, ClientSort> clientRepository;

    @Autowired
    private ClientService clientService;

    @Autowired
    private SortableCrudRepository<Apartment, ApartmentSort> apartmentRepository;

    private String url;

    private Client client1;
    private Client client2;
    private Client client3;

    private ClientEntity entity1;
    private ClientEntity entity2;
    private ClientEntity entity3;

    private Apartment apartment1;
    private Apartment apartment2;

    @BeforeAll
    static void beforeAll() {
        postgreSQLContainer.start();
    }

    @AfterAll
    static void afterAll() {
        postgreSQLContainer.stop();
    }

    @BeforeEach
    void setUp() {
        url = "http://localhost:" + port + "/clients";

        client1 = new Client(UUID.fromString("00000000-0000-0000-0000-000000000001"),
                "a-name-1", ClientStatus.NEW);
        client2 = new Client(UUID.fromString("00000000-0000-0000-0000-000000000002"),
                "c-name-2", ClientStatus.BANNED);
        client3 = new Client(UUID.fromString("00000000-0000-0000-0000-000000000003"),
                "b-name-3", ClientStatus.ADVANCED);

        entity1 = new ClientEntity(client1.getId(), client1.getName(), client1.getStatus(),
                client1.getApartments());
        entity2 = new ClientEntity(client2.getId(), client2.getName(), client2.getStatus(),
                client2.getApartments());
        entity3 = new ClientEntity(client3.getId(), client3.getName(), client3.getStatus(),
                client3.getApartments());

        apartment1 = new Apartment(UUID.fromString("00000000-0000-0000-0000-000000000003"),
                BigDecimal.TEN, BigInteger.TEN, true, ApartmentStatus.AVAILABLE);
        apartment2 = new Apartment(UUID.fromString("00000000-0000-0000-0000-000000000004"),
                BigDecimal.ONE, BigInteger.TWO, true, ApartmentStatus.AVAILABLE);
    }

    @AfterEach
    void tearDown() {
        Stream.of(client1, client2, client3)
                .filter(client -> clientRepository.has(client.getId()))
                .forEach(client -> clientRepository.delete(client.getId()));

        Stream.of(apartment1, apartment2)
                .filter(apartment -> apartmentRepository.has(apartment.getId()))
                .forEach(apartment -> apartmentRepository.delete(apartment.getId()));
    }

    @Test
    void save_NotExistingClient_ShouldReturnSavedClientEntity() {
        ClientDto clientDto = new ClientDto(client1.getId().toString(), client1.getName(), client1.getStatus().name());
        ClientEntity actual = restTemplate.postForObject(url, clientDto, ClientEntity.class);

        assertThat(actual).isEqualTo(entity1);
    }

    @Test
    void save_ExistingClient_ShouldReturnBadRequest() {
        clientRepository.save(client1);

        ClientDto clientDto = new ClientDto(client1.getId().toString(), client1.getName(), client1.getStatus().name());
        ResponseEntity<ErrorResponse> actual = restTemplate.postForEntity(url, clientDto, ErrorResponse.class);

        assertThat(actual.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(400));
    }

    @Test
    void delete_ExistingClient_ShouldDeleteEntity() {
        clientRepository.save(client1);

        restTemplate.delete(url + "/{id}", client1.getId().toString());

        assertThat(clientRepository.has(client1.getId())).isFalse();
    }

    @Test
    @SneakyThrows
    void delete_NotExistingClient_ShouldShouldReturnBadRequest() {
        ResponseEntity<ErrorResponse> response = restTemplate.exchange(url + "/{id}", HttpMethod.DELETE,
                null, ErrorResponse.class, client1.getId());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(400));
    }


    @Test
    @SneakyThrows
    void getById_ValidClient_ShouldReturnClientEntity() {
        clientRepository.save(client1);

        ClientEntity actual = restTemplate.getForObject(url + "/{id}", ClientEntity.class, client1.getId());

        assertThat(actual).isEqualTo(entity1);
    }

    @Test
    void getById_NotValidClient_ShouldReturnBadRequest() {
        ResponseEntity<ErrorResponse> actual = restTemplate.getForEntity(url + "/{id}", ErrorResponse.class,
                client1.getId());

        assertThat(actual.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(400));
    }

    @Test
    void findAllSorted_ClientId_ShouldReturnStoredByIdListClientEntity() {
        clientRepository.save(client1);
        clientRepository.save(client2);
        clientRepository.save(client3);

        List<ClientEntity> expected = List.of(entity1, entity2, entity3);
        ResponseEntity<List<ClientEntity>> actual = restTemplate.exchange(url + "?sort=id", HttpMethod.GET,
                null, new ParameterizedTypeReference<>() {
                });

        assertThat(actual.getBody()).isEqualTo(expected);
    }

    @Test
    void findAllSorted_ClientName_ShouldReturnStoredByNameListClientEntity() {
        clientRepository.save(client1);
        clientRepository.save(client2);
        clientRepository.save(client3);

        List<ClientEntity> expected = List.of(entity1, entity3, entity2);
        ResponseEntity<List<ClientEntity>> actual = restTemplate.exchange(url + "?sort=name", HttpMethod.GET,
                null, new ParameterizedTypeReference<>() {
                });

        assertThat(actual.getBody()).isEqualTo(expected);
    }

    @Test
    void findAllSorted_ClientStatus_ShouldReturnStoredByStatusListClientEntity() {
        clientRepository.save(client1);
        clientRepository.save(client2);
        clientRepository.save(client3);

        List<ClientEntity> expected = List.of(entity3, entity2, entity1);
        ResponseEntity<List<ClientEntity>> actual = restTemplate.exchange(url + "?sort=status", HttpMethod.GET,
                null, new ParameterizedTypeReference<>() {
                });

        assertThat(actual.getBody()).isEqualTo(expected);
    }

    @Test
    void calculatePrice_ExistingEntity_ShouldReturnPrice() {
        apartmentRepository.save(apartment1);
        apartmentRepository.save(apartment2);
        clientRepository.save(client1);

        clientService.checkIn(client1.getId(), apartment1.getId());
        clientService.checkIn(client1.getId(), apartment2.getId());

        BigDecimal expected = apartment1.getPrice().add(apartment2.getPrice());
        BigDecimal actual = restTemplate.getForObject(url + "/stay?clientId={id}", BigDecimal.class,
                client1.getId());

        assertThat(actual).isEqualByComparingTo(expected);
    }

    @Test
    void calculatePrice_NotExistingEntity_ShouldReturnBadRequest() {
        ResponseEntity<ErrorResponse> actual = restTemplate.getForEntity(url + "/stay?clientId={id}",
                ErrorResponse.class, client1.getId().toString());

        assertThat(actual.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(400));
    }

    @Test
    void checkOut_NotExistingClient_ShouldReturnBadRequest() {
        apartmentRepository.save(apartment1);

        ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                RequestEntity
                        .put("/clients/check-out")
                        .body(
                                Map.of(
                                        "clientId", client1.getId().toString(),
                                        "apartmentId", apartment1.getId().toString()
                                )
                        ), ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void checkOut_NotExistingApartments_ShouldReturnBadRequest() {
        clientRepository.save(client1);

        ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                RequestEntity
                        .put("/clients/check-out")
                        .body(
                                Map.of(
                                        "clientId", client1.getId().toString(),
                                        "apartmentId", apartment1.getId().toString()
                                )
                        ), ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void checkOut_ValidClientAndValidApartment_ShouldReturnStatusOK() {
        apartmentRepository.save(apartment1);
        clientService.save(client1);
        clientService.checkIn(client1.getId(), apartment1.getId());

        ResponseEntity<ClientEntity> response = restTemplate.exchange(
                RequestEntity
                        .put(url + "/check-out")
                        .body(
                                Map.of(
                                        "clientId", client1.getId().toString(),
                                        "apartmentId", apartment1.getId().toString()
                                )
                        ), ClientEntity.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(entity1);
    }

    @SneakyThrows
    @Test
    void checkIn_NotExistingClient_ShouldReturnBadRequest() {
        apartmentRepository.save(apartment1);

        ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                RequestEntity
                        .put("/clients/check-in")
                        .body(
                                Map.of(
                                        "clientId", client1.getId().toString(),
                                        "apartmentId", apartment1.getId().toString()
                                )
                        ), ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @SneakyThrows
    @Test
    void checkIn_NotExistingApartments_ShouldReturnBadRequest() {
        clientRepository.save(client1);

        ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                RequestEntity
                        .put("/clients/check-in")
                        .body(
                                Map.of(
                                        "clientId", client1.getId().toString(),
                                        "apartmentId", apartment1.getId().toString()
                                )
                        ), ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void checkIn_ValidApartmentAndValidClient_ShouldReturnStatusOK() {
        clientRepository.save(client1);
        apartmentRepository.save(apartment1);

        ResponseEntity<ClientEntity> response = restTemplate.exchange(
                RequestEntity
                        .put("/clients/check-in")
                        .body(
                                Map.of(
                                        "clientId", client1.getId().toString(),
                                        "apartmentId", apartment1.getId().toString()
                                )
                        ), ClientEntity.class);

        ClientEntity expected = new ClientEntity(client1.getId(), client1.getName(), client1.getStatus(),
                Set.of(
                        new ApartmentEntity(apartment1.getId(), apartment1.getPrice(),
                                apartment1.getCapacity(), false, ApartmentStatus.RESERVED)
                )
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(response.getBody()).apartments().iterator().next().id())
                .isEqualTo(expected.apartments().iterator().next().id());
    }
}
