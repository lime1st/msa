package msa.lime1st.composite.product.presentation;

import static reactor.core.publisher.Flux.empty;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import java.io.IOException;
import java.net.URI;
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
import msa.lime1st.util.exception.InvalidInputException;
import msa.lime1st.util.exception.NotFoundException;
import msa.lime1st.util.http.ApiUtil;
import msa.lime1st.util.http.HttpErrorInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

@Component
public class ProductCompositeIntegration implements ProductApi, RecommendationApi, ReviewApi {

    private static final Logger LOG = LoggerFactory.getLogger(ProductCompositeIntegration.class);

    private static final String PRODUCT_SERVICE_URL         = "http://product";
    private static final String RECOMMENDATION_SERVICE_URL  = "http://recommendation";
    private static final String REVIEW_SERVICE_URL          = "http://review";

    private final Scheduler publishEventScheduler;
    private final WebClient webClient;
    private final ObjectMapper mapper;
    private final StreamBridge streamBridge;
    private final ApiUtil apiUtil;

    public ProductCompositeIntegration(
        Scheduler publishEventScheduler,
        WebClient.Builder webClientBuilder,
        ObjectMapper mapper,
        StreamBridge streamBridge,
        ApiUtil apiUtil) {
        this.publishEventScheduler = publishEventScheduler;
        this.webClient = webClientBuilder.build();
        this.mapper = mapper;
        this.streamBridge = streamBridge;
        this.apiUtil = apiUtil;
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
    @Retry(name = "product")
    @TimeLimiter(name = "product")
    @CircuitBreaker(name = "product", fallbackMethod = "getProductFallbackValue")
    public Mono<ProductResponse> getProduct(
        int productId,
        int delay,
        int faultPercent
    ) {
        URI url = UriComponentsBuilder.fromUriString(
            PRODUCT_SERVICE_URL
                + "/product/{productId}?delay={delay}"
                + "&faultPercent={faultPercent}"
        ).build(productId, delay, faultPercent);
        LOG.debug("Will call the getProduct API on URL: {}", url);

        return webClient.get().uri(url)
            .retrieve()
            .bodyToMono(ProductResponse.class)
            .log(LOG.getName(), Level.FINE)
            // onErrorMap 메서드를 활용해 HTTP 계층의 예외를 자체 예외로 변경
            .onErrorMap(WebClientResponseException.class, this::handleException);
    }

    // fallback 메서드는 폴백을 지정한 메서드와 시그니처가 같아야 하고 마지막에 서킷 브레이커가 트리거하는 예외를 전달하기 위한 매개변수를 추가해야 한다.
    private Mono<ProductResponse> getProductFallbackValue(
        int productId,
        int delay,
        int faultPercent,
        CallNotPermittedException ex
    ) {

        LOG.warn("Creating a fail-fast fallback product for productId = {}, delay = {}, faultPercent = {} and exception = {} ",
            productId, delay, faultPercent, ex.toString());

        // 캐시에 값이 없는 경우를 시뮬레이션 하고자 특정 id 의 반환 값에 예외를 던진다.
        if (productId == 13) {
            String errMsg = "Product Id: " + productId + " not found in fallback cache!";
            LOG.warn(errMsg);
            throw new NotFoundException(errMsg);
        }

        // fallback 로직은 보통 내부 캐시 등의 다른 곳에서 같은 id(productId) 를 이용해 조회하도록 구현한다.
        // 여기서는 하드 코딩된 값을 반환한다.
        return Mono.just(ProductResponse.of(
            productId,
            "Fallback product" + productId,
            productId,
            apiUtil.getServiceAddress()));
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

        LOG.info("4. Post recommendation request: {}", request);

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

        String url = RECOMMENDATION_SERVICE_URL + "/recommendation?productId=" + productId;

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

        LOG.info("5. Post review request: {}", request);

        return Mono.fromCallable(() -> {
            sendMessage("reviews-out-0", Event.create(
                Type.CREATE, request.productId(), request));
            return request.toResponse();
        }).subscribeOn(publishEventScheduler);
    }

    @Override
    public Flux<ReviewResponse> getReviews(int productId) {

        String url = REVIEW_SERVICE_URL + "/review?productId=" + productId;

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

    private <K, T> void sendMessage(String bindingName, Event<K, T> event) {
        LOG.debug("Sending a {} message to {}", event.eventType(), bindingName);
        Message<Event<K, T>> message = MessageBuilder.withPayload(event)
            .setHeader("partitionKey", event.key())
            .build();
        streamBridge.send(bindingName, message);
    }

    private String getErrorMessage(WebClientResponseException ex) {
        try {
            return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).message();
        } catch (IOException e) {
            return ex.getMessage();
        }
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
}
