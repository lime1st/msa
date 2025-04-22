package msa.lime1st.composite.product.infrastructure;

import java.util.LinkedHashMap;
import java.util.Map;
import msa.lime1st.composite.product.presentation.ProductCompositeIntegration;
import org.springframework.boot.actuate.health.CompositeReactiveHealthContributor;
import org.springframework.boot.actuate.health.ReactiveHealthContributor;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HealthIndicator {

    private final ProductCompositeIntegration integration;

    public HealthIndicator(ProductCompositeIntegration integration) {
        this.integration = integration;
    }

    @Bean
    ReactiveHealthContributor coreServices() {
        final Map<String, ReactiveHealthIndicator> registry = new LinkedHashMap<>();

        registry.put("product", integration::getProductHealth);
        registry.put("recommendation", integration::getRecommendationHealth);
        registry.put("review", integration::getReviewHealth);

        return CompositeReactiveHealthContributor.fromMap(registry);
    }
}
