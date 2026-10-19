package com.hernanaguado.prices.config;

import com.hernanaguado.prices.application.GetApplicablePriceService;
import com.hernanaguado.prices.domain.port.in.GetApplicablePriceUseCase;
import com.hernanaguado.prices.domain.port.out.PriceRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers the framework-free application services as Spring beans.
 */
@Configuration(proxyBeanMethods = false)
class UseCaseConfiguration {

    @Bean
    GetApplicablePriceUseCase getApplicablePriceUseCase(PriceRepositoryPort priceRepository) {
        return new GetApplicablePriceService(priceRepository);
    }
}
