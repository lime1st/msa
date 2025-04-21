package msa.lime1st.api.composite.product;

public record ReviewSummary(
    int reviewId,
    String author,
    String subject,
    String content
) {

    public static ReviewSummary of(
        int reviewId,
        String author,
        String subject,
        String content
    ) {
        return new ReviewSummary(
            reviewId,
            author,
            subject,
            content
        );
    }
}
