package com.hernanaguado.prices.application;

import com.hernanaguado.prices.domain.exception.PriceNotFoundException;
import com.hernanaguado.prices.domain.model.Price;
import com.hernanaguado.prices.domain.policy.PriceSelectionPolicy;
import com.hernanaguado.prices.domain.port.in.GetApplicablePriceUseCase;
import com.hernanaguado.prices.domain.port.out.PriceRepositoryPort;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@RequiredArgsConstructor
public class GetApplicablePriceService implements GetApplicablePriceUseCase {

    private final PriceRepositoryPort priceRepository;

    @Override
    public Price getApplicablePrice(LocalDateTime applicationDate, Long productId, Long brandId) {
        return PriceSelectionPolicy
                .selectApplicable(priceRepository.findApplicable(applicationDate, productId, brandId))
                .orElseThrow(() -> new PriceNotFoundException(applicationDate, productId, brandId));
    }
}
