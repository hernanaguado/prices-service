package com.hernanaguado.prices.infrastructure.adapter.out.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "BRAND")
class BrandEntity {

    @Id
    private Long id;

    protected BrandEntity() {
    }

    public Long getId() {
        return id;
    }
}
