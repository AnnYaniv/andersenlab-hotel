package com.andersenlab.hotel.http;

import com.andersenlab.hotel.usecase.AdjustApartmentPriceUseCase;
import jakarta.servlet.http.HttpServletResponse;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public class AdjustServlet extends JsonServlet {

    private final AdjustApartmentPriceUseCase useCase;

    public AdjustServlet(AdjustApartmentPriceUseCase useCase) {
        this.useCase = useCase;
    }

    @Override
    Response put(String uri, Map<String, String> body) {
        UUID apartmentId = UUID.fromString(body.get("id"));
        BigDecimal price = new BigDecimal(body.get("price"));
        useCase.adjust(apartmentId, price);
        return new Response(HttpServletResponse.SC_ACCEPTED);
    }
}
