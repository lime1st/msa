package msa.lime1st.api.composite.product;

import java.util.List;

public record ProductAggregateRequest(
    int productId,
    String name,
    int weight,
    List<RecommendationSummary> recommendations,
    List<ReviewSummary> reviews,
    ServiceAddresses serviceAddresses
) {

    public static ProductAggregateRequest of(
        int productId,
        String name,
        int weight,
        List<RecommendationSummary> recommendations,
        List<ReviewSummary> reviews,
        ServiceAddresses serviceAddresses
    ) {
        return new ProductAggregateRequest(
            productId,
            name,
            weight,
            recommendations,
            reviews,
            serviceAddresses
        );
    }
}
