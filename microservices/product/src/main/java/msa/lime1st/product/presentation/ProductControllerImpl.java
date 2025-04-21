package msa.lime1st.product.presentation;

import msa.lime1st.api.core.product.ProductApi;
import msa.lime1st.api.core.product.ProductRequest;
import msa.lime1st.api.core.product.ProductResponse;
import msa.lime1st.api.exception.InvalidInputException;
import msa.lime1st.api.exception.NotFoundException;
import msa.lime1st.product.infrastructure.persistence.ProductDocument;
import msa.lime1st.product.infrastructure.persistence.ProductRepository;
import msa.lime1st.util.http.ApiUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductControllerImpl implements ProductApi {

    private static final Logger LOG = LoggerFactory.getLogger(ProductControllerImpl.class);

    private final ApiUtil apiUtil;
    private final ProductMapper mapper;
    private final ProductRepository repository;

    public ProductControllerImpl(
        ApiUtil apiUtil,
        ProductMapper mapper,
        ProductRepository repository
    ) {
        this.apiUtil = apiUtil;
        this.mapper = mapper;
        this.repository = repository;
    }

    @Override
    public ProductResponse postProduct(ProductRequest request) {
        try {
            ProductDocument document = mapper.requestToDocument(request);
            ProductDocument savedDocument = repository.save(document);

            LOG.info("create Product: document created for productId: {}",
                savedDocument.getProductId());
            return mapper.documentToResponse(savedDocument);
        } catch (DuplicateKeyException dke) {
            throw new InvalidInputException("Duplicate key, Product Id: " + request.productId());
        }
    }

    @Override
    public ProductResponse getProduct(int productId) {
        LOG.debug("/product return the found product for productId={}", productId);

        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        ProductDocument document = repository.findByProductId(productId)
            .orElseThrow(() -> new NotFoundException("No product found for productId: " + productId));

        ProductResponse response = mapper.documentToResponse(document)
            .withServiceAddress(apiUtil.getServiceAddress());

        LOG.debug("get product: found productId: {}", response.productId());

        return response;
    }

    @Override
    public void deleteProduct(int productId) {
        LOG.debug("delete product: tries to delete productId={}", productId);
        repository.findByProductId(productId)
            .ifPresent(repository::delete);
    }
}
