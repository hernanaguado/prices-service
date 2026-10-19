package com.hernanaguado.prices.infrastructure.adapter.in.rest;

import com.hernanaguado.prices.domain.exception.PriceNotFoundException;
import com.hernanaguado.prices.domain.model.Price;
import com.hernanaguado.prices.domain.port.in.GetApplicablePriceUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PriceController.class)
@Import(PriceRestMapperImpl.class)
class PriceControllerTest {

    private static final LocalDateTime APPLICATION_DATE = LocalDateTime.of(2020, 6, 14, 16, 0);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetApplicablePriceUseCase getApplicablePriceUseCase;

    @Test
    void returnsTheApplicablePrice() throws Exception {
        given(getApplicablePriceUseCase.getApplicablePrice(APPLICATION_DATE, 35455L, 1L)).willReturn(new Price(
                1L, 35455L, 2, LocalDateTime.of(2020, 6, 14, 15, 0), LocalDateTime.of(2020, 6, 14, 18, 30),
                1, new BigDecimal("25.45"), "EUR"));

        mockMvc.perform(get("/prices")
                        .queryParam("applicationDate", "2020-06-14T16:00:00")
                        .queryParam("productId", "35455")
                        .queryParam("brandId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.productId").value(35455))
                .andExpect(jsonPath("$.brandId").value(1))
                .andExpect(jsonPath("$.priceList").value(2))
                .andExpect(jsonPath("$.startDate").value("2020-06-14T15:00:00"))
                .andExpect(jsonPath("$.endDate").value("2020-06-14T18:30:00"))
                .andExpect(jsonPath("$.price").value(25.45))
                .andExpect(jsonPath("$.currency").value("EUR"));
    }

    @ParameterizedTest(name = "[{index}] applicationDate={0}, productId={1}, brandId={2}")
    @CsvSource(nullValues = "null", value = {
            "null,                35455, 1",
            "2020-06-14T16:00:00, null,  1",
            "2020-06-14T16:00:00, 35455, null",
            "14/06/2020,          35455, 1",
            "2020-06-14,          35455, 1",
            "2020-06-14T16:00:00, abc,   1",
            "2020-06-14T16:00:00, 35455, x",
            "2020-06-14T16:00:00, 0,     1",
            "2020-06-14T16:00:00, 35455, -1"
    })
    void rejectsInvalidParameters(String applicationDate, String productId, String brandId) throws Exception {
        var request = get("/prices");
        if (applicationDate != null) {
            request.queryParam("applicationDate", applicationDate);
        }
        if (productId != null) {
            request.queryParam("productId", productId);
        }
        if (brandId != null) {
            request.queryParam("brandId", brandId);
        }

        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.instance").value("/prices"));

        verifyNoInteractions(getApplicablePriceUseCase);
    }

    @Test
    void returnsNotFoundWhenNoPriceApplies() throws Exception {
        given(getApplicablePriceUseCase.getApplicablePrice(APPLICATION_DATE, 35455L, 1L))
                .willThrow(new PriceNotFoundException(APPLICATION_DATE, 35455L, 1L));

        mockMvc.perform(get("/prices")
                        .queryParam("applicationDate", "2020-06-14T16:00:00")
                        .queryParam("productId", "35455")
                        .queryParam("brandId", "1"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.detail").value(containsString("35455")));
    }

    @Test
    void hidesInternalDetailsOnUnexpectedErrors() throws Exception {
        given(getApplicablePriceUseCase.getApplicablePrice(APPLICATION_DATE, 35455L, 1L))
                .willThrow(new RuntimeException("database connection lost"));

        mockMvc.perform(get("/prices")
                        .queryParam("applicationDate", "2020-06-14T16:00:00")
                        .queryParam("productId", "35455")
                        .queryParam("brandId", "1"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.detail").value("Unexpected error"))
                .andExpect(jsonPath("$.trace").doesNotExist())
                .andExpect(jsonPath("$.exception").doesNotExist())
                .andExpect(content().string(not(containsString("database connection lost"))))
                .andExpect(content().string(not(containsString("RuntimeException"))));
    }
}
