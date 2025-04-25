package msa.lime1st.api.composite.product;

public record ServiceAddresses(
    String cmp,
    String pro,
    String rev,
    String rec
) {

    public static ServiceAddresses of(
        String cmp,
        String pro,
        String rev,
        String rec
    ) {
        return new ServiceAddresses(
            cmp,
            pro,
            rev,
            rec
        );
    }
}
