package msa.lime1st.api.composite.product;

public record ReviewSummary(
    int reviewId,
    String author,
    String subject
) {

    public static ReviewSummary of(
        int reviewId,
        String author,
        String subject
    ) {
        return new ReviewSummary(
            reviewId,
            author,
            subject
        );
    }
}
