package msa.lime1st.product.presentation;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import msa.lime1st.api.core.product.ProductApi;
import msa.lime1st.api.core.product.ProductRequest;
import msa.lime1st.api.core.product.ProductResponse;
import msa.lime1st.product.infrastructure.persistence.ProductDocument;
import msa.lime1st.product.infrastructure.persistence.ProductRepository;
import msa.lime1st.util.exception.InvalidInputException;
import msa.lime1st.util.exception.NotFoundException;
import msa.lime1st.util.http.ApiUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

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
    public Mono<ProductResponse> postProduct(ProductRequest request) {

        if (request.productId() < 1) {
            throw new InvalidInputException("Invalid productId: " + request.productId());
        }

        return repository.save(mapper.requestToDocument(request))
            .log(LOG.getName(), Level.FINE)
            .onErrorMap(
                DuplicateKeyException.class,
                ex -> new InvalidInputException(
                    "Duplicate key, Product Id: " + request.productId()))
            .map(mapper::documentToResponse);
    }

    @Override
    public Mono<ProductResponse> getProduct(
        int productId,
        int delay,
        int faultPercent
    ) {
        LOG.debug("/product return the found product for productId={}", productId);

        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        LOG.info("Will get product info for id = {}", productId);

        return repository.findByProductId(productId)
            .map(document -> throwErrorIfBadLuck(document, faultPercent))
            .delayElement(Duration.ofSeconds(delay))    // 지연 적용
            .switchIfEmpty(Mono.error(new NotFoundException("No product found for productId: " + productId)))
            .log(LOG.getName(), Level.FINE)
            .map(mapper::documentToResponse)
            .map(response ->
                response.withServiceAddress(apiUtil.getServiceAddress()));
    }

    @Override
    public Mono<Void> deleteProduct(int productId) {

        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        LOG.debug("delete product: tries to delete productId={}", productId);
        return repository.findByProductId(productId)
            .log(LOG.getName(), Level.FINE)
            .map(repository::delete)
            .flatMap(e -> e);
    }

    // 랜덤 실패 발생기
    private ProductDocument throwErrorIfBadLuck(ProductDocument document, int faultPercent) {

        if (faultPercent == 0) {
            return document;
        }

        int randomThreshold = ThreadLocalRandom.current().nextInt((100 - 1) + 1) + 1;

        if (faultPercent < randomThreshold) {
            LOG.debug("We got lucky, no error occurred, {} < {}", faultPercent, randomThreshold);
        } else {
            LOG.debug("Bad luck, an error occurred, {} >= {}", faultPercent, randomThreshold);
            throw new RuntimeException("Something went wrong...");
        }

        return document;
    }
}
