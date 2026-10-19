package com.hernanaguado.prices.domain.exception;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PriceNotFoundException extends RuntimeException {

    public PriceNotFoundException(LocalDateTime applicationDate, Long productId, Long brandId) {
        super("No applicable price for productId %d and brandId %d at %s"
                .formatted(productId, brandId, applicationDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
    }
}
