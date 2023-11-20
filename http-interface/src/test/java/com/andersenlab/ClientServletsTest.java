package com.andersenlab;

import com.andersenlab.hotel.HotelModule;

import com.andersenlab.hotel.common.reader.PropertyReaderFromFile;
import com.andersenlab.hotel.http.ServletStarter;
import com.andersenlab.hotel.model.Apartment;
import com.andersenlab.hotel.model.ApartmentEntity;
import com.andersenlab.hotel.model.ApartmentStatus;
import com.andersenlab.hotel.model.Client;
import com.andersenlab.hotel.model.ClientEntity;
import com.andersenlab.hotel.model.ClientSort;
import com.andersenlab.hotel.model.ClientStatus;
import com.andersenlab.hotel.common.service.ContextBuilder;
import com.andersenlab.hotel.repository.jdbc.JdbcConnector;
import com.andersenlab.hotel.service.CrudService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;

import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

class ClientServletsTest {

    private static final Logger LOG = LoggerFactory.getLogger(ClientServletsTest.class);
    private static String user;
    private static String password;

    final String url = "http://localhost:8080/clients";

    AtomicInteger integer = new AtomicInteger(0);

    ServletStarter starter;
    HotelModule context;
    ObjectMapper mapper = new ObjectMapper();

    private Client client1;
    private Client client2;
    private Client client3;

    private Apartment apartment1;
    private Apartment apartment2;

    @BeforeAll
    static void beforeAll() {
        final PropertyReaderFromFile reader = new PropertyReaderFromFile("application.properties");
        user = reader.readProperty("jdbc.user");
        password = reader.readProperty("jdbc.password");
    }

    @BeforeEach
    void setUp() {
        String db = "ht21-" + integer.incrementAndGet();
        JdbcConnector connector = new JdbcConnector("jdbc:h2:~/" + db, user, password)
                .migrate();

        context = new ContextBuilder().initJdbc(connector)
                .doRepositoryThreadSafe()
                .initServices()
                .initCheckInCheckOut(true)
                .build();

        starter = ServletStarter.forModule(context);
        starter.run();

        client1 = new Client(UUID.fromString("00000000-0000-0000-0000-000000000001"),
                "name-1", ClientStatus.NEW);

        client2 = new Client(UUID.fromString("00000000-0000-0000-0000-000000000002"),
                "name-2", ClientStatus.BANNED);

        client3 = new Client(UUID.fromString("00000000-0000-0000-0000-000000000003"),
                "name-3", ClientStatus.ADVANCED);

        apartment1 = new Apartment(UUID.fromString("00000000-0000-0000-0000-000000000003"),
                BigDecimal.TEN, BigInteger.TEN, true, ApartmentStatus.AVAILABLE);

        apartment2 = new Apartment(UUID.fromString("00000000-0000-0000-0000-000000000004"),
                BigDecimal.ONE, BigInteger.TWO, true, ApartmentStatus.AVAILABLE);
    }

    @AfterEach
    void tearDown() {
        try {
            CrudService<Apartment, ApartmentEntity> apartmentService = context.apartmentService();
            CrudService<Client, ClientEntity> clientService = context.clientService();

            clientService.delete(client1.getId());
            clientService.delete(client2.getId());
            clientService.delete(client3.getId());
            apartmentService.delete(apartment1.getId());
            apartmentService.delete(apartment2.getId());
        } catch (RuntimeException e) {
            LOG.warn("Failed to tear down {}", e.toString());
        }
        starter.stop();
    }

    @Test
    void delete_ExistingClient_ShouldReturnSC_NON_CONTENT() {
        context.clientService().save(client1);
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .method("DELETE", HttpRequest.BodyPublishers.ofString("{\"id\":\"" +
                            client1.getId() + "\"}"))
                    .build();

            HttpResponse<String> resp = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertThat(resp.statusCode()).isEqualTo(HttpServletResponse.SC_NO_CONTENT);
        } catch (URISyntaxException | IOException | InterruptedException e) {
            Assertions.fail(e.getMessage());
        }
    }

    @Test
    void delete_NotExistingClient_ShouldShouldReturnSC_BAD_REQUEST() {
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .method("DELETE", HttpRequest.BodyPublishers.ofString("{\"id\":\"" +
                            client1.getId() + "\"}"))
                    .build();

            HttpResponse<String> resp = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertThat(resp.statusCode()).isEqualTo(HttpServletResponse.SC_BAD_REQUEST);
        } catch (URISyntaxException | IOException | InterruptedException e) {
            Assertions.fail(e.getMessage());
        }
    }

    @Test
    @SneakyThrows
    void save_NotExistingClient_ShouldReturnSavedClientJson() {
        final String clientJson = clientWithTrimmedApartments(client1);

        final String expectedClient = mapper.writeValueAsString(
                new Client(client1.getId(), client1.getName(), ClientStatus.NEW)
        );

        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .method("POST", HttpRequest.BodyPublishers.ofString(clientJson))
                    .build();

            HttpResponse<String> resp = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertThat(resp.body()).isEqualTo(expectedClient);
        } catch (URISyntaxException | IOException | InterruptedException e) {
            Assertions.fail(e.getMessage());
        }
    }

    @Test
    @SneakyThrows
    void save_ExistingClient_ShouldReturnSC_BAD_REQUEST() {
        context.clientService().save(client1);
        final String clientJson = clientWithTrimmedApartments(client1);

        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .method("POST", HttpRequest.BodyPublishers.ofString(clientJson))
                    .build();

            HttpResponse<String> resp = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertThat(resp.statusCode()).isEqualTo(HttpServletResponse.SC_BAD_REQUEST);
        } catch (URISyntaxException | IOException | InterruptedException e) {
            Assertions.fail(e.getMessage());
        }
    }

    @Test
    @SneakyThrows
    void getById_ValidClient_ShouldReturnClientJson() {
        context.clientService().save(client1);

        final String expectedClient = mapper.writeValueAsString(
                new Client(client1.getId(), client1.getName(), ClientStatus.NEW)
        );

        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(url + "/" + client1.getId()))
                    .GET()
                    .build();

            HttpResponse<String> resp = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertThat(resp.body()).isEqualTo(expectedClient);
        } catch (URISyntaxException | IOException | InterruptedException e) {
            Assertions.fail(e.getMessage());
        }
    }

    @Test
    void getById_NotValidClient_ShouldReturnSC_BAD_REQUEST() {
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(url + "/" + client1.getId()))
                    .GET()
                    .build();

            HttpResponse<String> resp = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertThat(resp.statusCode()).isEqualTo(HttpServletResponse.SC_BAD_REQUEST);
        } catch (URISyntaxException | IOException | InterruptedException e) {
            Assertions.fail(e.getMessage());
        }
    }

    @Test
    void findAllSorted_ClientId_ShouldReturnStoredByIdInJson() {
        ClientSort sort = ClientSort.ID;
        CrudService<Client, ClientEntity> clientService = context.clientService();

        clientService.save(client1);
        clientService.save(client3);
        clientService.save(client2);

        List<Client> expected = Stream.of(client1, client2, client3)
                .sorted(sort.getComparator())
                .toList();

        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(url + "?sort=" + sort.name()))
                    .GET()
                    .build();

            HttpResponse<String> resp = client.send(request, HttpResponse.BodyHandlers.ofString());
            List<Client> actual = mapper.readValue(resp.body(), new TypeReference<>() {
            });

            assertThat(actual).isEqualTo(expected);
        } catch (URISyntaxException | IOException | InterruptedException e) {
            Assertions.fail(e.getMessage());
        }
    }

    @Test
    void findAllSorted_ClientName_ShouldReturnStoredByNameInJson() {
        ClientSort sort = ClientSort.NAME;
        CrudService<Client, ClientEntity> clientService = context.clientService();

        clientService.save(client1);
        clientService.save(client3);
        clientService.save(client2);

        List<Client> expected = Stream.of(client1, client2, client3)
                .sorted(sort.getComparator())
                .toList();

        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(url + "?sort=" + sort.name()))
                    .GET()
                    .build();

            HttpResponse<String> resp = client.send(request, HttpResponse.BodyHandlers.ofString());
            List<Client> actual = mapper.readValue(resp.body(), new TypeReference<>() {
            });

            assertThat(actual).isEqualTo(expected);
        } catch (URISyntaxException | IOException | InterruptedException e) {
            Assertions.fail(e.getMessage());
        }
    }

    @Test
    void findAllSorted_ClientStatus_ShouldReturnStoredByStatusInJson() {
        ClientSort sort = ClientSort.STATUS;
        CrudService<Client, ClientEntity> clientService = context.clientService();

        clientService.save(client1);
        clientService.save(client3);
        clientService.save(client2);

        List<Client> expected = Stream.of(client1, client2, client3)
                .sorted(sort.getComparator())
                .toList();

        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(url + "?sort=" + sort.name()))
                    .GET()
                    .build();

            HttpResponse<String> resp = client.send(request, HttpResponse.BodyHandlers.ofString());
            List<Client> actual = mapper.readValue(resp.body(), new TypeReference<>() {
            });

            assertThat(actual).isEqualTo(expected);
        } catch (URISyntaxException | IOException | InterruptedException e) {
            Assertions.fail(e.getMessage());
        }
    }

    @Test
    void calculatePrice_ExistingEntity_ShouldReturnPrice() {
        context.clientService().save(client1);
        context.apartmentService().save(apartment1);
        context.apartmentService().save(apartment2);
        context.checkInClientUseCase().checkIn(client1.getId(), apartment1.getId());
        context.checkInClientUseCase().checkIn(client1.getId(), apartment2.getId());

        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(url + "/stay?clientId=" + client1.getId()))
                    .GET()
                    .build();

            HttpResponse<String> resp = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertThat(new BigDecimal(resp.body())).isEqualByComparingTo(apartment1.getPrice().add(apartment2.getPrice()));
        } catch (URISyntaxException | IOException | InterruptedException e) {
            Assertions.fail(e.getMessage());
        }
    }

    @Test
    void calculatePrice_NotExistingEntity_ShouldReturnSC_BAD_REQUEST() {
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(url + "/stay?clientId=" + client1.getId()))
                    .GET()
                    .build();

            HttpResponse<String> resp = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertThat(resp.statusCode()).isEqualTo(HttpServletResponse.SC_BAD_REQUEST);
        } catch (URISyntaxException | IOException | InterruptedException e) {
            Assertions.fail(e.getMessage());
        }
    }


    @SneakyThrows
    private String clientWithTrimmedApartments(Client client) {
        String jsonClient1 = mapper.writeValueAsString(client);
        return jsonClient1.substring(0, jsonClient1.lastIndexOf(",\"apartments")) + "}";
    }
}