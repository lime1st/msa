package msa.lime1st.composite.product.presentation;


import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;
import msa.lime1st.api.composite.product.ProductAggregateRequest;
import msa.lime1st.api.composite.product.ProductAggregateResponse;
import msa.lime1st.api.composite.product.ProductCompositeApi;
import msa.lime1st.api.composite.product.RecommendationSummary;
import msa.lime1st.api.composite.product.ReviewSummary;
import msa.lime1st.api.composite.product.ServiceAddresses;
import msa.lime1st.api.core.product.ProductRequest;
import msa.lime1st.api.core.product.ProductResponse;
import msa.lime1st.api.core.recommendation.RecommendationRequest;
import msa.lime1st.api.core.recommendation.RecommendationResponse;
import msa.lime1st.api.core.review.ReviewRequest;
import msa.lime1st.api.core.review.ReviewResponse;
import msa.lime1st.util.http.ApiUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class ProductCompositeControllerImpl implements ProductCompositeApi {

    private static final Logger LOG = LoggerFactory.getLogger(ProductCompositeControllerImpl.class);

    private final ApiUtil apiUtil;
    private final ProductCompositeIntegration integration;

    public ProductCompositeControllerImpl(
        ApiUtil apiUtil,
        ProductCompositeIntegration integration
    ) {
        this.apiUtil = apiUtil;
        this.integration = integration;
    }

    @Override
    public Mono<Void> createProduct(ProductAggregateRequest aggregateRequest) {
        LOG.info("1. Creating product aggregate: {}", aggregateRequest);

        try {

            List<Mono> monoList = new ArrayList<>();

            LOG.debug("2. createCompositeProduct: creates a new composite entity for productId: {}",
                aggregateRequest.productId());

            ProductRequest productRequest = ProductRequest.of(
                aggregateRequest.productId(),
                aggregateRequest.name(),
                aggregateRequest.weight(),
                null
            );
            monoList.add(integration.postProduct(productRequest));

            if (aggregateRequest.recommendations() != null) {
                aggregateRequest.recommendations().forEach(rs -> {
                    RecommendationRequest recommendationRequest = RecommendationRequest.of(
                        aggregateRequest.productId(),
                        rs.recommendationId(),
                        rs.author(),
                        rs.rate(),
                        rs.content(),
                        null);
                    integration.postRecommendation(recommendationRequest);
                });
            }

            if (aggregateRequest.reviews() != null) {
                aggregateRequest.reviews().forEach(rs -> {
                    ReviewRequest reviewRequest = ReviewRequest.of(
                        aggregateRequest.productId(),
                        rs.reviewId(),
                        rs.author(),
                        rs.subject(),
                        rs.content(),
                        null
                    );
                    integration.postReview(reviewRequest);
                });
            }

            LOG.debug("createCompositeProduct: composite entities created for productId: {}",
                aggregateRequest.productId());

            return Mono.zip(r -> "", monoList.toArray(new Mono[0]))
                .doOnError(ex -> LOG.warn("createCompositeProduct failed: {}", ex.toString()))
                .then();

        } catch (RuntimeException re) {
            LOG.warn("createCompositeProduct failed", re);
            throw re;
        }
    }

    @Override
    public Mono<ProductAggregateResponse> getProduct(int productId) {
        LOG.info("Will get composite product info for product.id = {}", productId);
        return Mono.zip(
                values -> createProductAggregateResponse(
                    (ProductResponse) values[0],
                    (List<RecommendationResponse>) values[1],
                    (List<ReviewResponse>) values[2],
                    apiUtil.getServiceAddress()
                ),
                integration.getProduct(productId),
                integration.getRecommendations(productId).collectList(),
                integration.getReviews(productId).collectList())
            .doOnError(ex -> LOG.warn("getCompositeProduct failed: {}", ex.toString()))
            .log(LOG.getName(), Level.FINE);
    }

    @Override
    public Mono<Void> deleteProduct(int productId) {

        try {
            LOG.debug("deleteCompositeProduct: Deletes a product aggregate for productId: {}",
                productId);

            return Mono.zip(
                    r -> "",
                    integration.deleteProduct(productId),
                    integration.deleteRecommendations(productId),
                    integration.deleteReviews(productId)
                )
                .doOnError(ex -> LOG.warn("delete failed: {}", ex.toString()))
                .log(LOG.getName(), Level.FINE)
                .then();
        } catch (RuntimeException re) {
            LOG.warn("deleteCompositeProduct failed", re);
            throw re;
        }
    }

    private ProductAggregateResponse createProductAggregateResponse(
        ProductResponse response,
        List<RecommendationResponse> recommendations,
        List<ReviewResponse> reviews,
        String serviceAddress
    ) {

        // 1. Setup response info
        int productId = response.productId();
        String name = response.name();
        int weight = response.weight();

        // 2. Copy summary recommendation info, if available
        List<RecommendationSummary> recommendationSummaries =
            (recommendations == null) ? null : recommendations.stream()
                .map(r -> RecommendationSummary.of(
                    r.recommendationId(),
                    r.author(),
                    r.rate(),
                    r.content()))
                .collect(Collectors.toList());

        // 3. Copy summary review info, if available
        List<ReviewSummary> reviewSummaries =
            (reviews == null) ? null : reviews.stream()
                .map(r -> ReviewSummary.of(
                    r.reviewId(),
                    r.author(),
                    r.subject(),
                    r.content()))
                .collect(Collectors.toList());

        // 4. Create info regarding the involved microservices addresses
        String productAddress = response.serviceAddress();
        String reviewAddress = (reviews != null && !reviews.isEmpty()) ?
            reviews.get(0).serviceAddress() : "";
        String recommendationAddress = (recommendations != null && !recommendations.isEmpty()) ?
            recommendations.get(0).serviceAddress() : "";
        ServiceAddresses serviceAddresses = ServiceAddresses.of(
            serviceAddress,
            productAddress,
            reviewAddress,
            recommendationAddress
        );

        return ProductAggregateResponse.of(
            productId,
            name,
            weight,
            recommendationSummaries,
            reviewSummaries,
            serviceAddresses
        );
    }
}
