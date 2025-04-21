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

    public ProductResponse withServiceAddress (String serviceAddress) {
        return new ProductResponse(
            this.productId,
            this.name,
            this.weight,
            serviceAddress
        );
    }
}
