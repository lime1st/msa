package msa.lime1st.api.composite.product;

public record RecommendationSummary(
    int recommendationId,
    String author,
    int rate,
    String content
) {

    public static RecommendationSummary of(
        int recommendationId,
        String author,
        int rate,
        String content
    ) {
        return new RecommendationSummary(
            recommendationId,
            author,
            rate,
            content
        );
    }
}
