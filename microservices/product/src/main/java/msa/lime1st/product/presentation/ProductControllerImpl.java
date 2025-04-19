package msa.lime1st.product.presentation;

import msa.lime1st.api.core.product.ProductApi;
import msa.lime1st.api.core.product.ProductResponse;
import msa.lime1st.api.exception.InvalidInputException;
import msa.lime1st.api.exception.NotFoundException;
import msa.lime1st.util.http.ApiUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductControllerImpl implements ProductApi {

    private static final Logger LOG = LoggerFactory.getLogger(ProductControllerImpl.class);

    private final ApiUtil apiUtil;

    public ProductControllerImpl(ApiUtil apiUtil) {
        this.apiUtil = apiUtil;
    }

    @Override
    public ProductResponse getProduct(int productId) {
        LOG.debug("/product return the found product for productId={}", productId);

        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        if (productId == 13) {
            throw new NotFoundException("No product found for productId: " + productId);
        }

        return new ProductResponse(
            productId,
            "name-" + productId,
            123,
            apiUtil.getServiceAddress()
        );
    }
}
