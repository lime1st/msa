package msa.lime1st.composite.product.presentation;

import java.net.URL;
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
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class ProductCompositeControllerImpl implements ProductCompositeApi {

    private static final Logger LOG = LoggerFactory.getLogger(ProductCompositeControllerImpl.class);

    private final SecurityContext nullSecCtx = new SecurityContextImpl();

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
    public Mono<Void> postProduct(ProductAggregateRequest aggregateRequest) {
        LOG.info("1. Creating product aggregate: {}", aggregateRequest);

        try {
            LOG.debug("2. createCompositeProduct: creates a new composite entity for productId: {}",
                aggregateRequest.productId());

            List<Mono<?>> monoList = new ArrayList<>();

            monoList.add(getLogAuthorizationInfoMono());

            // Product 등록
            ProductRequest productRequest = ProductRequest.of(
                aggregateRequest.productId(),
                aggregateRequest.name(),
                aggregateRequest.weight(),
                null
            );
            monoList.add(integration.postProduct(productRequest));

            // Recommendations 등록: stream 사용
            if (aggregateRequest.recommendations() != null) {
                monoList.addAll(
                    aggregateRequest.recommendations().stream()
                        .map(rs -> RecommendationRequest.of(
                            aggregateRequest.productId(),
                            rs.recommendationId(),
                            rs.author(),
                            rs.rate(),
                            rs.content(),
                            null
                        ))
                        .map(integration::postRecommendation)
                        .toList()
                );
            }

            // Reviews 등록: forEach 사용
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
                    monoList.add(integration.postReview(reviewRequest));
                });
            }

            LOG.debug("createCompositeProduct: composite entities created for productId: {}",
                aggregateRequest.productId());

            /*
            Mono.zip()은 여러 개의 Mono 를 동시에 실행하고, 모든 Mono 가 완료될 때까지 기다립니다.
            모든 Mono 가 성공하면, 그 결과들을 조합해서 새로운 결과를 만들 수 있는데, 여기선 r -> "" 람다를 통해 결과는 무시하고 빈 문자열만 리턴하고 있어요.
            마지막 .then()은 Mono<Void>를 반환하며, 결과는 무시하고 성공 여부만 신경 쓰겠다는 의미입니다.
            즉, 이 부분은: "모든 작업이 완료될 때까지 기다린 뒤, 아무 결과도 반환하지 않음"을 의미하는 코드입니다.
             */
            return Mono.zip(monoList, result -> "")
                .doOnError(ex -> LOG.warn("createCompositeProduct failed: {}", ex.toString()))
                .then();

        } catch (RuntimeException re) {
            LOG.warn("createCompositeProduct failed", re);
            throw re;
        }
    }

    @Override
    public Mono<ProductAggregateResponse> getProduct(
        int productId,
        int delay,
        int faultPercent
    ) {
        LOG.info("Will get composite product info for product.id = {}", productId);

        Mono<ProductResponse> productMono = integration.getProduct(
            productId,
            delay,
            faultPercent
        );
        Mono<List<RecommendationResponse>> recommendationsMono = integration.getRecommendations(productId)
            .collectList();
        Mono<List<ReviewResponse>> reviewsMono = integration.getReviews(productId).collectList();

        return Mono.zip(
                getSecurityContextMono(),
                productMono,
                recommendationsMono,
                reviewsMono)
            .map(tuple -> createProductAggregateResponse(
                tuple.getT1(),
                tuple.getT2(),
                tuple.getT3(),
                tuple.getT4(),
                apiUtil.getServiceAddress()
            ))
            .doOnError(ex -> LOG.warn("getCompositeProduct failed: {}", ex.toString()))
            .log(LOG.getName(), Level.FINE);
    }

    @Override
    public Mono<Void> deleteProduct(int productId) {

        try {
            LOG.debug("deleteCompositeProduct: Deletes a product aggregate for productId: {}",
                productId);

            /*
            Mono.when(...)은 여러 Mono<Void>를 실행하고, 결과 없이 성공 여부만 확인할 때 적합합니다.
            Mono.zip(...).then()과 기능적으로 같지만, 목적이 더 명확하게 표현되어있습니다.
             */
            return Mono.when(
                    getLogAuthorizationInfoMono(),
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
        SecurityContext context,
        ProductResponse response,
        List<RecommendationResponse> recommendations,
        List<ReviewResponse> reviews,
        String serviceAddress
    ) {
        logAuthorizationInfo(context);

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

    private Mono<SecurityContext> getLogAuthorizationInfoMono() {
        return getSecurityContextMono().doOnNext(this::logAuthorizationInfo);
    }

    private Mono<SecurityContext> getSecurityContextMono() {
        return ReactiveSecurityContextHolder.getContext().defaultIfEmpty(nullSecCtx);
    }

    private void logAuthorizationInfo(SecurityContext sc) {
        if (sc != null && sc.getAuthentication() != null
            && sc.getAuthentication() instanceof JwtAuthenticationToken) {
            Jwt jwtToken = ((JwtAuthenticationToken) sc.getAuthentication()).getToken();
            logAuthorizationInfo(jwtToken);
        } else {
            LOG.warn("No JWT based Authentication supplied, running tests are we?");
        }
    }

    private void logAuthorizationInfo(Jwt jwt) {
        if (jwt == null) {
            LOG.warn("No JWT supplied, running tests are we?");
        } else {
            if (LOG.isDebugEnabled()) {
                URL issuer = jwt.getIssuer();
                List<String> audience = jwt.getAudience();
                Object subject = jwt.getClaims().get("sub");
                Object scopes = jwt.getClaims().get("scope");
                Object expires = jwt.getClaims().get("exp");

                LOG.debug(
                    "Authorization info: Subject: {}, scopes: {}, expires {}: issuer: {}, audience: {}",
                    subject, scopes, expires, issuer, audience);
            }
        }
    }
}
