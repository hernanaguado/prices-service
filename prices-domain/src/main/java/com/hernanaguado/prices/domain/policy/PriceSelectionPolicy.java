package com.hernanaguado.prices.domain.policy;

import com.hernanaguado.prices.domain.model.Price;

import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;

/**
 * Chooses the price to apply among the candidates valid at a given date: the highest priority wins
 * and, on a priority tie, the one with the most recent start date wins. On a full tie the first candidate wins,
 * which is the one the repository returns first.
 */
public final class PriceSelectionPolicy {

    private static final Comparator<Price> PRECEDENCE = Comparator
            .comparing(Price::priority)
            .thenComparing(Price::startDate);

    private PriceSelectionPolicy() {
    }

    public static Optional<Price> selectApplicable(Collection<Price> candidates) {
        return candidates.stream().max(PRECEDENCE);
    }
}
