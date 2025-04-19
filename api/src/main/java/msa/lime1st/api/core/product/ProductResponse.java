package msa.lime1st.api.core.product;

public record ProductResponse(
    int productId,
    String name,
    int weight,
    String serviceAddress
) {

    public static ProductResponse of (
        int productId,
        String name,
        int weight,
        String serviceAddress
    ) {
        return new ProductResponse(
            productId,
            name,
            weight,
            serviceAddress
        );
    }
}
