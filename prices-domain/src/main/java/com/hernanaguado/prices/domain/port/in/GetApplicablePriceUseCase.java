package com.hernanaguado.prices.domain.port.in;

import com.hernanaguado.prices.domain.model.Price;

import java.time.LocalDateTime;

public interface GetApplicablePriceUseCase {

    Price getApplicablePrice(LocalDateTime applicationDate, Long productId, Long brandId);
}
