package com.hernanaguado.prices.application;

import com.hernanaguado.prices.domain.exception.PriceNotFoundException;
import com.hernanaguado.prices.domain.model.Price;
import com.hernanaguado.prices.domain.port.out.PriceRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class GetApplicablePriceServiceTest {

    private static final LocalDateTime APPLICATION_DATE = LocalDateTime.of(2020, 6, 14, 16, 0);
    private static final Long PRODUCT_ID = 35455L;
    private static final Long BRAND_ID = 1L;

    @Mock
    private PriceRepositoryPort priceRepository;

    private GetApplicablePriceService service;

    @BeforeEach
    void setUp() {
        service = new GetApplicablePriceService(priceRepository);
    }

    @Test
    void returnsTheSelectedCandidate() {
        // given
        Price base = price(1, 0, LocalDateTime.of(2020, 6, 14, 0, 0), "35.50");
        Price promotion = price(2, 1, LocalDateTime.of(2020, 6, 14, 15, 0), "25.45");
        given(priceRepository.findApplicable(APPLICATION_DATE, PRODUCT_ID, BRAND_ID))
                .willReturn(List.of(base, promotion));

        // when
        Price result = service.getApplicablePrice(APPLICATION_DATE, PRODUCT_ID, BRAND_ID);

        // then
        assertThat(result).isEqualTo(promotion);
    }

    @Test
    void throwsWhenNoPriceApplies() {
        // given
        given(priceRepository.findApplicable(APPLICATION_DATE, PRODUCT_ID, BRAND_ID)).willReturn(List.of());

        // when
        Throwable thrown = catchThrowable(() -> service.getApplicablePrice(APPLICATION_DATE, PRODUCT_ID, BRAND_ID));

        // then
        assertThat(thrown)
                .isInstanceOf(PriceNotFoundException.class)
                .hasMessageContaining("productId 35455")
                .hasMessageContaining("brandId 1")
                .hasMessageContaining("2020-06-14T16:00:00");
    }

    private static Price price(int priceList, int priority, LocalDateTime startDate, String amount) {
        return new Price(BRAND_ID, PRODUCT_ID, priceList, startDate, LocalDateTime.of(2020, 12, 31, 23, 59, 59),
                priority, new BigDecimal(amount), "EUR");
    }
}
