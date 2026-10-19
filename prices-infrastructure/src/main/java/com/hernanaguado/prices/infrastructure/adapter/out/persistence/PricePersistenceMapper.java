package com.hernanaguado.prices.infrastructure.adapter.out.persistence;

import com.hernanaguado.prices.domain.model.Price;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
interface PricePersistenceMapper {

    @Mapping(target = "brandId", source = "brand.id")
    Price toDomain(PriceEntity entity);

    List<Price> toDomain(List<PriceEntity> entities);
}
