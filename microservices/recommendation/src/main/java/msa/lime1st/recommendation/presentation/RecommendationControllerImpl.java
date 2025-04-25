package msa.lime1st.recommendation.presentation;

import java.util.logging.Level;
import msa.lime1st.api.core.recommendation.RecommendationApi;
import msa.lime1st.api.core.recommendation.RecommendationRequest;
import msa.lime1st.api.core.recommendation.RecommendationResponse;
import msa.lime1st.util.exception.InvalidInputException;
import msa.lime1st.recommendation.infrastructure.persistence.RecommendationRepository;
import msa.lime1st.util.http.ApiUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class RecommendationControllerImpl implements RecommendationApi {

    private static final Logger LOG = LoggerFactory.getLogger(RecommendationControllerImpl.class);

    private final ApiUtil apiUtil;
    private final RecommendationMapper mapper;
    private final RecommendationRepository repository;

    public RecommendationControllerImpl(ApiUtil apiUtil, RecommendationMapper mapper,
        RecommendationRepository repository) {
        this.apiUtil = apiUtil;
        this.mapper = mapper;
        this.repository = repository;
    }

    @Override
    public Mono<RecommendationResponse> postRecommendation(RecommendationRequest request) {

        if (request.productId() < 1) {
            throw new InvalidInputException("Invalid productId: " + request.productId());
        }

        return repository.save(mapper.requestToDocument(request))
            .log(LOG.getName(), Level.FINE)
            .onErrorMap(
                DuplicateKeyException.class,
                ex -> new InvalidInputException(
                    "Duplicate key, Product Id: " + request.productId() +
                        ", Recommendation Id: " + request.recommendationId()))
            .map(mapper::documentToResponse);
    }

    @Override
    public Flux<RecommendationResponse> getRecommendations(int productId) {

        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        LOG.info("Will get recommendations for product with id={}", productId);

        return repository.findByProductId(productId)
            .log(LOG.getName(), Level.FINE)
            .map(mapper::documentToResponse)
            .map(response ->
                response.withServiceAddress(apiUtil.getServiceAddress()));
    }

    @Override
    public Mono<Void> deleteRecommendations(int productId) {

        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        LOG.debug(
            "deleteRecommendations: tries to delete recommendations for the product with productId: {}",
            productId);
        return repository.deleteAll(repository.findByProductId(productId));
    }
}
