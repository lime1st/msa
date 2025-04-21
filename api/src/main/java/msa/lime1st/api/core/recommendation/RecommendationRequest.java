package msa.lime1st.api.core.recommendation;

public record RecommendationRequest(
    Integer productId,
    Integer recommendationId,
    String author,
    Integer rate,
    String content,
    String serviceAddress
) {

    public static RecommendationRequest of (
        Integer productId,
        Integer recommendationId,
        String author,
        Integer rate,
        String content,
        String serviceAddress
    ) {
        return new RecommendationRequest(
            productId,
            recommendationId,
            author,
            rate,
            content,
            serviceAddress
        );
    }
}
