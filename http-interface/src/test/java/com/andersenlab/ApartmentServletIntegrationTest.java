package com.andersenlab;

import com.andersenlab.hotel.HotelModule;
import com.andersenlab.hotel.common.service.ContextBuilder;
import com.andersenlab.hotel.http.ServletStarter;
import com.andersenlab.hotel.model.Apartment;
import com.andersenlab.hotel.model.ApartmentEntity;
import com.andersenlab.hotel.service.impl.ApartmentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

import static com.andersenlab.hotel.model.ApartmentStatus.RESERVED;

class ApartmentServletIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(ApartmentServletIntegrationTest.class);

    private ApartmentService apartmentService;
    private final String uri = "http://localhost:8080/apartments";
    private final UUID id = UUID.fromString("00000000-0000-0000-0000-000000000000");
    private ObjectMapper objectMapper;
    private ServletStarter servletStarter;

    @BeforeEach
    void setUp() {
        HotelModule context = new ContextBuilder().initJpa("test_persistence")
                .doRepositoryThreadSafe()
                .initServices()
                .initCheckInCheckOut(true)
                .build();

        apartmentService = (ApartmentService) context.apartmentService();
        objectMapper = new ObjectMapper();
        servletStarter = ServletStarter.forModule(context);
        servletStarter.run();
    }

    @AfterEach
    void teardown() {
        try {
            apartmentService.delete(id);
        } catch (RuntimeException e) {
            LOG.warn("Tear down with exception {}", e.toString());
        }
        servletStarter.stop();
    }

    @Test
    @Tag("GET")
    void getApartmentByNonExistingId_shouldRespondBadRequest() throws IOException, InterruptedException {
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri + "/" + id))
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        Assertions.assertThat(apartmentService.has(id)).isFalse();
        Assertions.assertThat(response.statusCode()).isEqualTo(400);
    }

    @Test
    @Tag("GET")
    void getApartmentById_shouldRespondStatusOk() throws IOException, InterruptedException {
        apartmentService.save(new Apartment(id));
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri + "/" + id))
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        Assertions.assertThat(apartmentService.has(id)).isTrue();
        Assertions.assertThat(response.statusCode()).isEqualTo(200);
    }

    @Test
    @Tag("GET")
    void getListOfApartments_shouldReturnStatusCode200() throws IOException, InterruptedException {
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri + "?sort=ID"))
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        Assertions.assertThat(response.statusCode()).isEqualTo(200);
    }

    @Test
    @Tag("DELETE")
    void deleteApartmentWithExistingId_shouldReturnStatusCode200() throws IOException, InterruptedException {
        apartmentService.save(new Apartment(id));
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .method("DELETE", HttpRequest.BodyPublishers
                        .ofString(String.format("""
                                        {
                                            "id" : "%s"
                                        }
                                        """,
                                id)
                        )
                )
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        Assertions.assertThat(apartmentService.has(id)).isFalse();
        Assertions.assertThat(response.statusCode()).isEqualTo(200);
    }

    @Test
    @Tag("DELETE")
    void deleteApartmentNonWithExistingId_shouldReturnBadRequest() throws IOException, InterruptedException {
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .method("DELETE", HttpRequest.BodyPublishers
                        .ofString(String.format("""
                                        {
                                            "id" : "%s"
                                        }
                                        """,
                                id))
                )
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        Assertions.assertThat(apartmentService.has(id)).isFalse();
        Assertions.assertThat(response.statusCode()).isEqualTo(400);
    }

    @Test
    @Tag("POST")
    void addApartment_shouldAddApartmentAndReturnStatusCodeOk() throws IOException, InterruptedException {
        final String jsonBody = objectMapper.writeValueAsString(
                new Apartment(id, new BigDecimal(34), BigInteger.valueOf(4), true, RESERVED)
        );
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        Assertions.assertThat(apartmentService.has(id)).isTrue();
        Assertions.assertThat(response.statusCode()).isEqualTo(200);
    }

    @Test
    @Tag("POST")
    void addExistingApartment_shouldReturnBadRequest() throws IOException, InterruptedException {
        apartmentService.save(new Apartment(id));
        final String jsonBody = objectMapper.writeValueAsString(
                new Apartment(id, new BigDecimal(34), BigInteger.valueOf(4), true, RESERVED)
        );
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        Assertions.assertThat(apartmentService.has(id)).isTrue();
        Assertions.assertThat(response.statusCode()).isEqualTo(400);
    }

    @Test
    @Tag("adjust-apartment-price")
    void adjustApartmentPrice_shouldReturnStatusOk() throws IOException, InterruptedException {
        apartmentService.save(new Apartment(id, new BigDecimal(34), BigInteger.valueOf(4), true, RESERVED));
        final BigDecimal newPrice = new BigDecimal(245);
        final String jsonBody = objectMapper.writeValueAsString(
                new Apartment(id, newPrice, BigInteger.valueOf(4), true, RESERVED)
        );
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri + "/adjust"))
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        ApartmentEntity apartment = apartmentService.getById(id);
        Assertions.assertThat(apartment.price()).isEqualByComparingTo(newPrice);
        Assertions.assertThat(response.statusCode()).isEqualTo(202);
    }

    @Test
    @Tag("adjust-apartment-price")
    void notAdjustApartmentPriceIfIdNotExist_shouldReturnBadRequest() throws IOException, InterruptedException {
        final String jsonBody = objectMapper.writeValueAsString(
                new Apartment(id, new BigDecimal(678), BigInteger.valueOf(4), true, RESERVED)
        );
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri + "/adjust"))
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        Assertions.assertThat(response.statusCode()).isEqualTo(400);
    }
}