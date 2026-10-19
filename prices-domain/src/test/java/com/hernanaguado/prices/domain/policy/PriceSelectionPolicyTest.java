package com.hernanaguado.prices.domain.policy;

import com.hernanaguado.prices.domain.model.Price;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class PriceSelectionPolicyTest {

    @Test
    void selectsHighestPriority() {
        // given
        Price base = price(1, 0, LocalDateTime.of(2020, 6, 14, 0, 0));
        Price promotion = price(2, 1, LocalDateTime.of(2020, 6, 14, 15, 0));

        // when
        Optional<Price> selectedFromPromotionFirst = PriceSelectionPolicy.selectApplicable(List.of(promotion, base));
        Optional<Price> selectedFromBaseFirst = PriceSelectionPolicy.selectApplicable(List.of(base, promotion));

        // then
        assertThat(selectedFromPromotionFirst).contains(promotion);
        assertThat(selectedFromBaseFirst).contains(promotion);
    }

    @Test
    void breaksPriorityTieByMostRecentStartDate() {
        // given
        Price older = price(3, 1, LocalDateTime.of(2020, 6, 15, 0, 0));
        Price newer = price(4, 1, LocalDateTime.of(2020, 6, 15, 16, 0));

        // when
        Optional<Price> selectedFromNewerFirst = PriceSelectionPolicy.selectApplicable(List.of(newer, older));
        Optional<Price> selectedFromOlderFirst = PriceSelectionPolicy.selectApplicable(List.of(older, newer));

        // then
        assertThat(selectedFromNewerFirst).contains(newer);
        assertThat(selectedFromOlderFirst).contains(newer);
    }

    @Test
    void breaksFullTieByCandidateOrder() {
        // given
        Price first = price(5, 1, LocalDateTime.of(2020, 6, 15, 0, 0));
        Price second = price(6, 1, LocalDateTime.of(2020, 6, 15, 0, 0));

        // when
        Optional<Price> selected = PriceSelectionPolicy.selectApplicable(List.of(first, second));

        // then
        assertThat(selected).contains(first);
    }

    @Test
    void returnsEmptyWhenThereAreNoCandidates() {
        // given
        List<Price> candidates = List.of();

        // when
        Optional<Price> selected = PriceSelectionPolicy.selectApplicable(candidates);

        // then
        assertThat(selected).isEmpty();
    }

    private static Price price(int priceList, int priority, LocalDateTime startDate) {
        return new Price(1L, 35455L, priceList, startDate, LocalDateTime.of(2020, 12, 31, 23, 59, 59),
                priority, new BigDecimal("35.50"), "EUR");
    }
}
