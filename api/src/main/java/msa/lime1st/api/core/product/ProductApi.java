package msa.lime1st.api.core.product;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

public interface ProductApi {

    @GetMapping("/product/{productId}")
    ProductResponse getProduct(@PathVariable("productId") int productId);
}
