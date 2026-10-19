package com.hernanaguado.prices.application;

import com.hernanaguado.prices.domain.exception.PriceNotFoundException;
import com.hernanaguado.prices.domain.model.Price;
import com.hernanaguado.prices.domain.policy.PriceSelectionPolicy;
import com.hernanaguado.prices.domain.port.in.GetApplicablePriceUseCase;
import com.hernanaguado.prices.domain.port.out.PriceRepositoryPort;

import java.time.LocalDateTime;

public class GetApplicablePriceService implements GetApplicablePriceUseCase {

    private final PriceRepositoryPort priceRepository;

    public GetApplicablePriceService(PriceRepositoryPort priceRepository) {
        this.priceRepository = priceRepository;
    }

    @Override
    public Price getApplicablePrice(LocalDateTime applicationDate, Long productId, Long brandId) {
        return PriceSelectionPolicy
                .selectApplicable(priceRepository.findApplicable(applicationDate, productId, brandId))
                .orElseThrow(() -> new PriceNotFoundException(applicationDate, productId, brandId));
    }
}
