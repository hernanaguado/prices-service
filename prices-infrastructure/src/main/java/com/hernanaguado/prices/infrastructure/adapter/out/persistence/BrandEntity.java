package com.hernanaguado.prices.infrastructure.adapter.out.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "BRAND")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class BrandEntity {

    @Id
    private Long id;
}
