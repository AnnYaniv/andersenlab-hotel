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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ClientEndToEndTests {

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

    @BeforeEach
    void setUp() {
        url = "http://localhost:" + port;

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
        clientRepository.delete(client1.getId());
        clientRepository.delete(client2.getId());
        clientRepository.delete(client3.getId());
        apartmentRepository.delete(apartment1.getId());
        apartmentRepository.delete(apartment2.getId());
    }

    @Test
    void save_NotExistingClient_ShouldReturnSavedClientEntity() {
        ClientDto clientDto = new ClientDto(client1.getId().toString(), client1.getName(), client1.getStatus().name());
        ClientEntity actual = restTemplate.postForObject(url + "/clients", clientDto, ClientEntity.class);

        assertThat(actual).isEqualTo(entity1);
    }

    @Test
    void save_NotExistingClient_ShouldReturnBadRequest() {
        clientRepository.save(client1);

        ClientDto clientDto = new ClientDto(client1.getId().toString(), client1.getName(), client1.getStatus().name());
        ResponseEntity<ErrorResponse> actual = restTemplate.postForEntity(url + "/clients", clientDto, ErrorResponse.class);

        assertThat(actual.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(400));
    }

    @Test
    void delete_ExistingClient_ShouldDeleteEntity() {
        clientRepository.save(client1);

        restTemplate.delete(url + "/clients/" + client1.getId().toString());

        assertThat(clientRepository.has(client1.getId())).isFalse();
    }

    @Test
    @SneakyThrows
    void delete_NotExistingClient_ShouldShouldReturnBadRequest() {
        RequestEntity<String> request = new RequestEntity<>(HttpMethod.DELETE,
                new URI(url + "/clients/" + client1.getId().toString()));
        ResponseEntity<ErrorResponse> response = restTemplate.exchange(request, ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(400));
    }


    @Test
    @SneakyThrows
    void getById_ValidClient_ShouldReturnClientEntity() {
        clientRepository.save(client1);

        ClientEntity actual = restTemplate.getForObject(url + "/clients/" + client1.getId().toString(),
                ClientEntity.class);

        assertThat(actual).isEqualTo(entity1);
    }

    @Test
    void getById_NotValidClient_ShouldReturnBadRequest() {
        ResponseEntity<ErrorResponse> actual = restTemplate.postForEntity(url + "/clients", client1.getId(), ErrorResponse.class);

        assertThat(actual.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(400));
    }

    @Test
    void findAllSorted_ClientId_ShouldReturnStoredByIdListClientEntity() {
        clientRepository.save(client1);
        clientRepository.save(client2);
        clientRepository.save(client3);

        List<ClientEntity> expected = List.of(entity1, entity2, entity3);
        ResponseEntity<List<ClientEntity>> actual = restTemplate.exchange(url + "/clients?sort=id",
                HttpMethod.GET, null, new ParameterizedTypeReference<>() {
                });

        assertThat(actual.getBody()).isEqualTo(expected);
    }

    @Test
    void findAllSorted_ClientName_ShouldReturnStoredByNameListClientEntity() {
        clientRepository.save(client1);
        clientRepository.save(client2);
        clientRepository.save(client3);

        List<ClientEntity> expected = List.of(entity1, entity3, entity2);
        ResponseEntity<List<ClientEntity>> actual = restTemplate.exchange(url + "/clients?sort=name",
                HttpMethod.GET, null, new ParameterizedTypeReference<>() {
                });

        assertThat(actual.getBody()).isEqualTo(expected);
    }

    @Test
    void findAllSorted_ClientStatus_ShouldReturnStoredByStatusListClientEntity() {
        clientRepository.save(client1);
        clientRepository.save(client2);
        clientRepository.save(client3);

        List<ClientEntity> expected = List.of(entity3, entity2, entity1);
        ResponseEntity<List<ClientEntity>> actual = restTemplate.exchange(url + "/clients?sort=status",
                HttpMethod.GET, null, new ParameterizedTypeReference<>() {
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
        BigDecimal actual = restTemplate.getForObject(url + "/clients/stay?clientId=" + client1.getId().toString(), BigDecimal.class);

        assertThat(actual).isEqualByComparingTo(expected);
    }

    @Test
    void calculatePrice_NotExistingEntity_ShouldReturnBadRequest() {
        ResponseEntity<ErrorResponse> actual = restTemplate.getForEntity(
                String.format("%s/clients/stay?clientId=%s", url, client1.getId().toString()),
                ErrorResponse.class
        );

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
        clientRepository.save(client1);
        apartmentRepository.save(apartment1);
        clientService.checkIn(client1.getId(), apartment1.getId());

        ResponseEntity<ClientEntity> response = restTemplate.exchange(
                RequestEntity
                        .put("/clients/check-out")
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
        assertThat(response.getBody()).isEqualTo(expected);
    }
}
