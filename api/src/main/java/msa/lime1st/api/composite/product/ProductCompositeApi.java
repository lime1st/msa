package msa.lime1st.api.composite.product;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

public interface ProductCompositeApi {

    @GetMapping("/product-composite/{productId}")
    ProductAggregateResponse getProduct(@PathVariable("productId") int productId);
}
