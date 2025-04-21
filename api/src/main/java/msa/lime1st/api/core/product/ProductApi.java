package msa.lime1st.api.core.product;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface ProductApi {

    /**
     * Sample usage, see below.
     * <p>
     * curl -X POST $HOST:$PORT/product \
     *   -H "Content-Type: application/json" --data \
     *   '{"productId":123,"name":"product 123","weight":123}'
     *
     * @param request A JSON representation of the new product
     * @return A JSON representation of the newly created product
     */
    @PostMapping("/product")
    ProductResponse postProduct(@RequestBody ProductRequest request);

    /**
     * Sample usage: "curl $HOST:$PORT/product/1".
     *
     * @param productId id of the product
     * @return the product, if found, else null
     */
    @GetMapping("/product/{productId}")
    ProductResponse getProduct(@PathVariable("productId") int productId);

    /**
     * Sample usage: "curl -X DELETE $HOST:$PORT/product/1".
     *
     * @param productId id of the product
     */
    @DeleteMapping("/product/{productId}")
    void deleteProduct(@PathVariable("productId") int productId);
}
