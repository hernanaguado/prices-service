package com.hernanaguado.prices.infrastructure.adapter.in.rest;

import com.hernanaguado.prices.domain.port.in.GetApplicablePriceUseCase;
import com.hernanaguado.prices.infrastructure.adapter.in.rest.api.PricesApi;
import com.hernanaguado.prices.infrastructure.adapter.in.rest.model.PriceResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
class PriceController implements PricesApi {

    private final GetApplicablePriceUseCase getApplicablePriceUseCase;
    private final PriceRestMapper priceMapper;

    PriceController(GetApplicablePriceUseCase getApplicablePriceUseCase, PriceRestMapper priceMapper) {
        this.getApplicablePriceUseCase = getApplicablePriceUseCase;
        this.priceMapper = priceMapper;
    }

    @Override
    public ResponseEntity<PriceResponse> getApplicablePrice(LocalDateTime applicationDate, Long productId, Long brandId) {
        return ResponseEntity.ok(priceMapper.toResponse(
                getApplicablePriceUseCase.getApplicablePrice(applicationDate, productId, brandId)));
    }
}
