package msa.lime1st.api.core.review;

public record ReviewResponse(
    int productId,
    int reviewId,
    String author,
    String subject,
    String content,
    String serviceAddress
) {

    public static ReviewResponse of(
        int productId,
        int reviewId,
        String author,
        String subject,
        String content,
        String serviceAddress
    ) {
        return new ReviewResponse(
            productId,
            reviewId,
            author,
            subject,
            content,
            serviceAddress
        );
    }

    public ReviewResponse withServiceAddress(String serviceAddress) {
        return new ReviewResponse(
            this.productId,
            this.reviewId,
            this.author,
            this.subject,
            this.content,
            serviceAddress
        );
    }
}
