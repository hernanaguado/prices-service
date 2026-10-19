package com.hernanaguado.prices.domain.port.out;

import com.hernanaguado.prices.domain.model.Price;

import java.time.LocalDateTime;
import java.util.List;

public interface PriceRepositoryPort {

    /**
     * Returns every price of the product and brand whose validity range contains the given date, ordered from
     * highest to lowest precedence, so that candidates that fully tie always come in the same order.
     */
    List<Price> findApplicable(LocalDateTime applicationDate, Long productId, Long brandId);
}
