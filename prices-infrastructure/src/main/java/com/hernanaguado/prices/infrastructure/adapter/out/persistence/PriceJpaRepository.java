package com.hernanaguado.prices.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

interface PriceJpaRepository extends JpaRepository<PriceEntity, Long> {

    List<PriceEntity> findByBrand_IdAndProductIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Long brandId, Long productId, LocalDateTime startDateLimit, LocalDateTime endDateLimit);

    /**
     * Prices of the brand product whose validity range [startDate, endDate] contains the given date.
     */
    default List<PriceEntity> findApplicable(LocalDateTime applicationDate, Long productId, Long brandId) {
        return findByBrand_IdAndProductIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                brandId, productId, applicationDate, applicationDate);
    }
}
