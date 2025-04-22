package msa.lime1st.api.core.product;

public record ProductRequest(
    Integer productId,
    String name,
    Integer weight,
    String serviceAddress
) {

    public static ProductRequest of (
        Integer productId,
        String name,
        Integer weight,
        String serviceAddress
    ) {
        return new ProductRequest(
            productId,
            name,
            weight,
            serviceAddress
        );
    }

    public ProductResponse toResponse() {
        return ProductResponse.of(
            productId,
            name,
            weight,
            serviceAddress
        );
    }
}
