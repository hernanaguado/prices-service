package com.hernanaguado.prices.infrastructure.adapter.out.persistence;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.data.domain.Sort.Order.desc;

interface PriceJpaRepository extends JpaRepository<PriceEntity, Long> {

    /**
     * Highest priority first, then the most recent start date, then the most recently created price, so the
     * order is always deterministic and never left to the database.
     */
    Sort PRECEDENCE = Sort.by(desc("priority"), desc("startDate"), desc("id"));

    List<PriceEntity> findByBrand_IdAndProductIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Long brandId, Long productId, LocalDateTime startDateLimit, LocalDateTime endDateLimit, Sort sort);

    /**
     * Prices of the brand product whose validity range [startDate, endDate] contains the given date, in
     * {@link #PRECEDENCE} order.
     */
    default List<PriceEntity> findApplicable(LocalDateTime applicationDate, Long productId, Long brandId) {
        return findByBrand_IdAndProductIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                brandId, productId, applicationDate, applicationDate, PRECEDENCE);
    }
}
