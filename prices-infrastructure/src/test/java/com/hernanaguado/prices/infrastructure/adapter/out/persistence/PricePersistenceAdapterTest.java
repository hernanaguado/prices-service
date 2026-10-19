package com.hernanaguado.prices.infrastructure.adapter.out.persistence;

import com.hernanaguado.prices.domain.model.Price;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=none")
@Import({PricePersistenceAdapter.class, PricePersistenceMapperImpl.class})
class PricePersistenceAdapterTest {

    private static final Long BRAND_ID = 1L;
    private static final Long PRODUCT_ID = 35455L;

    @Autowired
    private PricePersistenceAdapter adapter;

    @Test
    void returnsEveryPriceValidAtTheDateByPrecedence() {
        List<Price> prices = adapter.findApplicable(LocalDateTime.of(2020, 6, 14, 16, 0), PRODUCT_ID, BRAND_ID);

        assertThat(prices).containsExactly(
                new Price(BRAND_ID, PRODUCT_ID, 2, LocalDateTime.of(2020, 6, 14, 15, 0),
                        LocalDateTime.of(2020, 6, 14, 18, 30), 1, new BigDecimal("25.45"), "EUR"),
                new Price(BRAND_ID, PRODUCT_ID, 1, LocalDateTime.of(2020, 6, 14, 0, 0),
                        LocalDateTime.of(2020, 12, 31, 23, 59, 59), 0, new BigDecimal("35.50"), "EUR"));
    }

    @Test
    void includesBothEndsOfTheValidityRange() {
        assertThat(adapter.findApplicable(LocalDateTime.of(2020, 6, 14, 15, 0), PRODUCT_ID, BRAND_ID))
                .extracting(Price::priceList).containsExactly(2, 1);
        assertThat(adapter.findApplicable(LocalDateTime.of(2020, 6, 14, 18, 30), PRODUCT_ID, BRAND_ID))
                .extracting(Price::priceList).containsExactly(2, 1);
    }

    @Test
    void returnsNothingOutsideAnyRange() {
        assertThat(adapter.findApplicable(LocalDateTime.of(2020, 6, 13, 23, 59, 59), PRODUCT_ID, BRAND_ID)).isEmpty();
    }

    @Test
    void returnsNothingForUnknownProductOrBrand() {
        LocalDateTime date = LocalDateTime.of(2020, 6, 14, 10, 0);

        assertThat(adapter.findApplicable(date, 1L, BRAND_ID)).isEmpty();
        assertThat(adapter.findApplicable(date, PRODUCT_ID, 2L)).isEmpty();
    }
}
