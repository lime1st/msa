package msa.lime1st.composite.product.presentation;

import java.util.List;
import java.util.stream.Collectors;
import msa.lime1st.api.composite.product.ProductAggregateResponse;
import msa.lime1st.api.composite.product.ProductCompositeApi;
import msa.lime1st.api.composite.product.RecommendationSummary;
import msa.lime1st.api.composite.product.ReviewSummary;
import msa.lime1st.api.composite.product.ServiceAddresses;
import msa.lime1st.api.core.product.ProductResponse;
import msa.lime1st.api.core.recommendation.RecommendationResponse;
import msa.lime1st.api.core.review.ReviewResponse;
import msa.lime1st.api.exception.NotFoundException;
import msa.lime1st.util.http.ApiUtil;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductCompositeControllerImpl implements ProductCompositeApi {

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
    public ProductAggregateResponse getProduct(int productId) {
        ProductResponse response = integration.getProduct(productId);
        if (response == null) {
            throw new NotFoundException("No response found for productId: " + productId);
        }

        List<RecommendationResponse> recommendations = integration.getRecommendations(productId);

        List<ReviewResponse> reviews = integration.getReviews(productId);

        return createProductAggregate(
            response,
            recommendations,
            reviews,
            apiUtil.getServiceAddress()
        );
    }

    private ProductAggregateResponse createProductAggregate(
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
                .map(r -> new RecommendationSummary(
                    r.recommendationId(),
                    r.author(),
                    r.rate()))
                .collect(Collectors.toList());

        // 3. Copy summary review info, if available
        List<ReviewSummary> reviewSummaries =
            (reviews == null) ? null : reviews.stream()
                .map(r -> new ReviewSummary(
                    r.reviewId(),
                    r.author(),
                    r.subject()))
                .collect(Collectors.toList());

        // 4. Create info regarding the involved microservices addresses
        String productAddress = response.serviceAddress();
        String reviewAddress = (reviews != null && !reviews.isEmpty()) ?
            reviews.get(0).serviceAddress() : "";
        String recommendationAddress = (recommendations != null && !recommendations.isEmpty()) ?
            recommendations.get(0).serviceAddress() : "";
        ServiceAddresses serviceAddresses = new ServiceAddresses(
            serviceAddress,
            productAddress,
            reviewAddress,
            recommendationAddress
        );

        return new ProductAggregateResponse(
            productId,
            name,
            weight,
            recommendationSummaries,
            reviewSummaries,
            serviceAddresses
        );
    }
}
