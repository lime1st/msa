package msa.lime1st.composite.product.presentation;

import static reactor.core.publisher.Flux.empty;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.logging.Level;
import msa.lime1st.api.core.product.ProductApi;
import msa.lime1st.api.core.product.ProductRequest;
import msa.lime1st.api.core.product.ProductResponse;
import msa.lime1st.api.core.recommendation.RecommendationApi;
import msa.lime1st.api.core.recommendation.RecommendationRequest;
import msa.lime1st.api.core.recommendation.RecommendationResponse;
import msa.lime1st.api.core.review.ReviewApi;
import msa.lime1st.api.core.review.ReviewRequest;
import msa.lime1st.api.core.review.ReviewResponse;
import msa.lime1st.api.event.Event;
import msa.lime1st.api.event.Event.Type;
import msa.lime1st.api.exception.InvalidInputException;
import msa.lime1st.api.exception.NotFoundException;
import msa.lime1st.util.http.HttpErrorInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

@Component
public class ProductCompositeIntegration implements ProductApi, RecommendationApi, ReviewApi {

    private static final Logger LOG = LoggerFactory.getLogger(ProductCompositeIntegration.class);

    private final WebClient webClient;
    private final ObjectMapper mapper;

    private final String productServiceUrl;
    private final String recommendationServiceUrl;
    private final String reviewServiceUrl;

    private final StreamBridge streamBridge;

    private final Scheduler publishEventScheduler;

    public ProductCompositeIntegration(
        Scheduler publishEventScheduler,

        WebClient.Builder webClient,
        ObjectMapper mapper,
        StreamBridge streamBridge,

        @Value("${app.product.host}") String productServiceHost,
        @Value("${app.product.port}") int productServicePort,

        @Value("${app.recommendation.host}") String recommendationServiceHost,
        @Value("${app.recommendation.port}") int recommendationServicePort,

        @Value("${app.review.host}") String reviewServiceHost,
        @Value("${app.review.port}") int reviewServicePort
    ) {
        this.publishEventScheduler = publishEventScheduler;
        this.webClient = webClient.build();
        this.mapper = mapper;
        this.streamBridge = streamBridge;

        productServiceUrl = "http://" + productServiceHost + ":" + productServicePort + "/product";
        recommendationServiceUrl =
            "http://" + recommendationServiceHost + ":" + recommendationServicePort
                + "/recommendation";
        reviewServiceUrl = "http://" + reviewServiceHost + ":" + reviewServicePort + "/review";
    }

    @Override
    public Mono<ProductResponse> postProduct(ProductRequest request) {

        LOG.info("3. Post product request: {}", request);

        return Mono.fromCallable(() -> {
            sendMessage("products-out-0", Event.create(
                Type.CREATE,
                request.productId(),
                request));
            return request.toResponse();
        }).subscribeOn(publishEventScheduler);
    }

    @Override
    public Mono<ProductResponse> getProduct(int productId) {
        String url = productServiceUrl + "/" + productId;
        LOG.debug("Will call the getProduct API on URL: {}", url);

        return webClient.get().uri(url)
            .retrieve()
            .bodyToMono(ProductResponse.class)
            .log(LOG.getName(), Level.FINE)
            // onErrorMap 메서드를 활용해 HTTP 계층의 예외를 자체 예외로 변경
            .onErrorMap(WebClientResponseException.class, this::handleException);
    }

    @Override
    public Mono<Void> deleteProduct(int productId) {
        return Mono.fromRunnable(() ->
                sendMessage("products-out-0", Event.create(
                    Type.DELETE,
                    productId,
                    null)))
            .subscribeOn(publishEventScheduler)
            .then();
    }

    @Override
    public Mono<RecommendationResponse> postRecommendation(RecommendationRequest request) {

        LOG.info("Post recommendation request: {}", request);

        return Mono.fromCallable(() -> {
            sendMessage("recommendations-out-0", Event.create(
                Type.CREATE,
                request.productId(),
                request));
            return request.toResponse();
        }).subscribeOn(publishEventScheduler);
    }

    @Override
    public Flux<RecommendationResponse> getRecommendations(int productId) {

        String url = recommendationServiceUrl + "/recommendation?productId=" + productId;

        LOG.debug("Will call the getRecommendations API on URL: {}", url);

        // product 와 달리 recommendation, review 에서는 예외가 발생하더라도 composite service 가
        // 일부 결과를 반환할 수 있도록 전체 요청이 실패한 것으로 처리하지 않는다.
        // 예외를 전파하는 대신 가능한 한 많은 정보를 호출자에게 돌려주기 위해 onErrorResume(error->empty())
        // 메서드를 사용해 빈 recommendation 혹은 review 목록을 반환한다.
        return webClient.get().uri(url)
            .retrieve()
            .bodyToFlux(RecommendationResponse.class)
            .log(LOG.getName(), Level.FINE)
            .onErrorResume(error -> empty());
    }

    @Override
    public Mono<Void> deleteRecommendations(int productId) {

        return Mono.fromRunnable(() ->
                sendMessage("recommendations-out-0", Event.create(
                    Type.DELETE,
                    productId,
                    null)))
            .subscribeOn(publishEventScheduler)
            .then();
    }

    @Override
    public Mono<ReviewResponse> postReview(ReviewRequest request) {

        LOG.info("Post review request: {}", request);

        return Mono.fromCallable(() -> {
            sendMessage("reviews-out-0", Event.create(
                Type.CREATE, request.productId(), request));
            return request.toResponse();
        }).subscribeOn(publishEventScheduler);
    }

    @Override
    public Flux<ReviewResponse> getReviews(int productId) {

        String url = reviewServiceUrl + "/review?productId=" + productId;

        LOG.debug("Will call the getReviews API on URL: {}", url);

        // Return an empty result if something goes wrong to make it possible for the composite service to return partial responses
        return webClient.get().uri(url)
            .retrieve()
            .bodyToFlux(ReviewResponse.class)
            .log(LOG.getName(), Level.FINE)
            .onErrorResume(error -> empty());
    }

    @Override
    public Mono<Void> deleteReviews(int productId) {
        return Mono.fromRunnable(() ->
                sendMessage("reviews-out-0", Event.create(Type.DELETE, productId, null)))
            .subscribeOn(publishEventScheduler)
            .then();
    }

    public Mono<Health> getProductHealth() {
        return getHealth(productServiceUrl);
    }

    public Mono<Health> getRecommendationHealth() {
        return getHealth(recommendationServiceUrl);
    }

    public Mono<Health> getReviewHealth() {
        return getHealth(reviewServiceUrl);
    }

    private Mono<Health> getHealth(String url) {

        url += "/actuator/health";
        LOG.debug("Will call the Health API on URL: {}", url);

        return webClient.get().uri(url)
            .retrieve()
            .bodyToMono(String.class)
            .map(s -> new Health.Builder()
                .up()
                .build())
            .onErrorResume(ex -> Mono.just(new Health.Builder()
                .down(ex)
                .build()))
            .log(LOG.getName(), Level.FINE);
    }

    private void sendMessage(String bindingName, Event event) {
        LOG.debug("Sending a {} message to {}", event.eventType(), bindingName);
        Message<Event> message = MessageBuilder.withPayload(event)
            .setHeader("partitionKey", event.key())
            .build();
        streamBridge.send(bindingName, message);
    }

    private Throwable handleException(Throwable ex) {

        if (!(ex instanceof WebClientResponseException wcre)) {
            LOG.warn("Got a unexpected error: {}, will rethrow it", ex.toString());
            return ex;
        }

        HttpStatusCode statusCode = wcre.getStatusCode();
        if (statusCode.equals(HttpStatus.NOT_FOUND)) {
            return new NotFoundException(getErrorMessage(wcre));
        } else if (statusCode.equals(HttpStatus.UNPROCESSABLE_ENTITY)) {
            return new InvalidInputException(getErrorMessage(wcre));
        }
        LOG.warn("Got an unexpected HTTP error: {}, will rethrow it", wcre.getStatusCode());
        LOG.warn("Error body: {}", wcre.getResponseBodyAsString());
        return ex;
    }

    private String getErrorMessage(WebClientResponseException ex) {
        try {
            return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).message();
        } catch (IOException e) {
            return ex.getMessage();
        }
    }
}
