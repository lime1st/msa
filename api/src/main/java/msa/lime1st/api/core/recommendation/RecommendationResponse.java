package msa.lime1st.api.core.recommendation;

public record RecommendationResponse (
    int productId,
    int recommendationId,
    String author,
    int rate,
    String content,
    String serviceAddress
) {

    public static RecommendationResponse of (
        int productId,
        int recommendationId,
        String author,
        int rate,
        String content,
        String serviceAddress
    ) {
        return new RecommendationResponse(
            productId,
            recommendationId,
            author,
            rate,
            content,
            serviceAddress
        );
    }

    public RecommendationResponse withServiceAddress (String serviceAddress) {
        return new RecommendationResponse(
            this.productId,
            this.recommendationId,
            this.author,
            this.rate,
            this.content,
            serviceAddress
        );
    }
}
