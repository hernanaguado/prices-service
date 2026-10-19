package com.hernanaguado.prices.infrastructure.adapter.out.persistence;

import com.hernanaguado.prices.domain.model.Price;
import com.hernanaguado.prices.domain.port.out.PriceRepositoryPort;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
class PricePersistenceAdapter implements PriceRepositoryPort {

    private final PriceJpaRepository priceJpaRepository;
    private final PricePersistenceMapper priceMapper;

    PricePersistenceAdapter(PriceJpaRepository priceJpaRepository, PricePersistenceMapper priceMapper) {
        this.priceJpaRepository = priceJpaRepository;
        this.priceMapper = priceMapper;
    }

    @Override
    public List<Price> findApplicable(LocalDateTime applicationDate, Long productId, Long brandId) {
        return priceMapper.toDomain(priceJpaRepository.findApplicable(applicationDate, productId, brandId));
    }
}
