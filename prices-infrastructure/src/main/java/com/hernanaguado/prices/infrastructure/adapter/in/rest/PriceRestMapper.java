package com.hernanaguado.prices.infrastructure.adapter.in.rest;

import com.hernanaguado.prices.domain.model.Price;
import com.hernanaguado.prices.infrastructure.adapter.in.rest.model.PriceResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
interface PriceRestMapper {

    PriceResponse toResponse(Price price);
}
