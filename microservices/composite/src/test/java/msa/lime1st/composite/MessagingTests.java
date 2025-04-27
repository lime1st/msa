package msa.lime1st.composite;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import msa.lime1st.api.composite.product.ProductAggregateRequest;
import msa.lime1st.api.composite.product.RecommendationSummary;
import msa.lime1st.api.composite.product.ReviewSummary;
import msa.lime1st.api.core.product.ProductRequest;
import msa.lime1st.api.core.recommendation.RecommendationRequest;
import msa.lime1st.api.core.review.ReviewRequest;
import msa.lime1st.api.event.Event;
import msa.lime1st.api.event.Event.Type;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@SpringBootTest(
    webEnvironment = RANDOM_PORT,
    classes = {TestSecurityConfig.class},
    properties = {
        "spring.security.oauth2.resourceserver.jwt.issuer-uri=",
        "spring.main.allow-bean-definition-overriding=true",
        "spring.cloud.stream.defaultBinder=rabbit"
    })
@Import({TestChannelBinderConfiguration.class})
public class MessagingTests {

    private static final Logger LOG = LoggerFactory.getLogger(MessagingTests.class);

    @Autowired
    private WebTestClient client;

    @Autowired
    private OutputDestination target;

    @BeforeEach
    void setUp() {
        purgeMessages("products");
        purgeMessages("recommendations");
        purgeMessages("reviews");
    }

    @Test
    void createCompositeProduct1() {

        ProductAggregateRequest request = ProductAggregateRequest.of(
            1, "name", 1,
            null, null, null
        );
        postAndVerifyProduct(request);

        final List<String> productMessages = getMessages("products");
        final List<String> recommendationMessages = getMessages("recommendations");
        final List<String> reviewMessages = getMessages("reviews");

        // 대기열에서 꺼낸 product 이벤트가 하나인지 확인
        assertEquals(1, productMessages.size());

        Event<Integer, ProductRequest> expectedEvent = Event.create(
            Type.CREATE,
            request.productId(),
            ProductRequest.of(
                request.productId(),
                request.name(),
                request.weight(),
                null
            )
        );
        assertThat(productMessages.get(0), is(IsSameEvent.sameEventExceptCreatedAt(expectedEvent)));

        // 대기열에 recommendation 및 review 이벤트가 없는 것을 확인한다.
        assertEquals(0, recommendationMessages.size());
        assertEquals(0, reviewMessages.size());
    }

    @Test
    void createCompositeProduct2() {

        ProductAggregateRequest request = ProductAggregateRequest.of(
            1, "name", 1,
            Collections.singletonList(RecommendationSummary.of(1, "a", 1, "c")),
            Collections.singletonList(ReviewSummary.of(1, "a", "s", "c")), null);
        postAndVerifyProduct(request);

        final List<String> productMessages = getMessages("products");
        final List<String> recommendationMessages = getMessages("recommendations");
        final List<String> reviewMessages = getMessages("reviews");

        // Assert one create product event queued up
        assertEquals(1, productMessages.size());

        Event<Integer, ProductRequest> expectedProductEvent =
            Event.create(Type.CREATE, request.productId(),
                ProductRequest.of(request.productId(), request.name(), request.weight(), null));
        assertThat(productMessages.get(0),
            is(IsSameEvent.sameEventExceptCreatedAt(expectedProductEvent)));

        // Assert one create recommendation event queued up
        assertEquals(1, recommendationMessages.size());

        RecommendationSummary rs = request.recommendations().get(0);
        Event<Integer, RecommendationRequest> expectedRecommendationEvent =
            Event.create(Type.CREATE, request.productId(),
                RecommendationRequest.of(request.productId(), rs.recommendationId(), rs.author(),
                    rs.rate(), rs.content(), null));
        assertThat(recommendationMessages.get(0),
            is(IsSameEvent.sameEventExceptCreatedAt(expectedRecommendationEvent)));

        // Assert one create review event queued up
        assertEquals(1, reviewMessages.size());

        ReviewSummary rev = request.reviews().get(0);
        Event<Integer, ReviewRequest> expectedReviewEvent =
            Event.create(Type.CREATE, request.productId(),
                ReviewRequest.of(request.productId(), rev.reviewId(), rev.author(), rev.subject(),
                    rev.content(), null));
        assertThat(reviewMessages.get(0),
            is(IsSameEvent.sameEventExceptCreatedAt(expectedReviewEvent)));
    }

    @Test
    void deleteCompositeProduct() {
        deleteAndVerifyProduct(1, HttpStatus.ACCEPTED);

        final List<String> productMessages = getMessages("products");
        final List<String> recommendationMessages = getMessages("recommendations");
        final List<String> reviewMessages = getMessages("reviews");

        // Assert one delete product event queued up
        assertEquals(1, productMessages.size());

        Event<Integer, ProductRequest> expectedProductEvent = Event.create(Type.DELETE, 1, null);
        assertThat(productMessages.get(0),
            is(IsSameEvent.sameEventExceptCreatedAt(expectedProductEvent)));

        // Assert one delete recommendation event queued up
        assertEquals(1, recommendationMessages.size());

        Event<Integer, RecommendationRequest> expectedRecommendationEvent = Event.create(
            Type.DELETE, 1, null);
        assertThat(recommendationMessages.get(0),
            is(IsSameEvent.sameEventExceptCreatedAt(expectedRecommendationEvent)));

        // Assert one delete review event queued up
        assertEquals(1, reviewMessages.size());

        Event<Integer, ReviewRequest> expectedReviewEvent = Event.create(Type.DELETE, 1, null);
        assertThat(reviewMessages.get(0),
            is(IsSameEvent.sameEventExceptCreatedAt(expectedReviewEvent)));
    }

    private void purgeMessages(String bindingName) {
        getMessages(bindingName);
    }

    private List<String> getMessages(String bindingName) {
        List<String> messages = new ArrayList<>();
        boolean anyMoreMessages = true;

        while (anyMoreMessages) {
            Message<byte[]> message = getMessage(bindingName);

            if (message == null) {
                anyMoreMessages = false;
            } else {
                messages.add(new String(message.getPayload()));
            }
        }
        return messages;
    }

    private Message<byte[]> getMessage(String bindingName) {
        try {
            return target.receive(0, bindingName);
        } catch (NullPointerException npe) {
            // receive 메서드가 호출될 때 대상 객체의 messageQueues 멤버 변수에 큐가 포함되어 있지 않으면 NPE가 발생합니다.
            LOG.error("getMessage() received a NPE with binding = {}", bindingName);
            return null;
        }
    }

    private void postAndVerifyProduct(ProductAggregateRequest request) {
        client.post()
            .uri("/product-composite")
            .body(Mono.just(request), ProductAggregateRequest.class)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.ACCEPTED);
    }

    private void deleteAndVerifyProduct(int productId, HttpStatus expectedStatus) {
        client.delete()
            .uri("/product-composite/" + productId)
            .exchange()
            .expectStatus().isEqualTo(expectedStatus);
    }
}
