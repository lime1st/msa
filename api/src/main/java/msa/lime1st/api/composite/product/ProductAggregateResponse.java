package msa.lime1st.api.composite.product;

import java.util.List;

public record ProductAggregateResponse (
    int productId,
    String name,
    int weight,
    List<RecommendationSummary> recommendations,
    List<ReviewSummary> reviews,
    ServiceAddresses serviceAddresses
) {

    public static ProductAggregateResponse of(
        int productId,
        String name,
        int weight,
        List<RecommendationSummary> recommendations,
        List<ReviewSummary> reviews,
        ServiceAddresses serviceAddresses
    ) {
        return new ProductAggregateResponse(
            productId,
            name,
            weight,
            recommendations,
            reviews,
            serviceAddresses
        );
    }
}
