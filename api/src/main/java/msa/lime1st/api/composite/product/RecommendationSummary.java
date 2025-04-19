package msa.lime1st.api.composite.product;

public record RecommendationSummary(
    int recommendationId,
    String author,
    int rate
) {

    public static RecommendationSummary of(
        int recommendationId,
        String author,
        int rate
    ) {
        return new RecommendationSummary(
            recommendationId,
            author,
            rate
        );
    }
}
