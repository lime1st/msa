package msa.lime1st.api.core.review;

public record ReviewRequest(
    Integer productId,
    Integer reviewId,
    String author,
    String subject,
    String content,
    String serviceAddress
) {

    public static ReviewRequest of (
        Integer productId,
        Integer reviewId,
        String author,
        String subject,
        String content,
        String serviceAddress
    ) {
        return new ReviewRequest(
            productId,
            reviewId,
            author,
            subject,
            content,
            serviceAddress
        );
    }
}
