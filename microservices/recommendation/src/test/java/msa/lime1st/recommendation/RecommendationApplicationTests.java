package msa.lime1st.recommendation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.function.Consumer;
import msa.lime1st.api.core.recommendation.RecommendationRequest;
import msa.lime1st.api.event.Event;
import msa.lime1st.api.event.Event.Type;
import msa.lime1st.api.exception.InvalidInputException;
import msa.lime1st.recommendation.infrastructure.persistence.MongoDbTestBase;
import msa.lime1st.recommendation.infrastructure.persistence.RecommendationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RecommendationApplicationTests extends MongoDbTestBase {

    @Autowired
    private WebTestClient client;

    @Autowired
    private RecommendationRepository repository;

    @Autowired
    private Consumer<Event<Integer, RecommendationRequest>> messageProcessor;

    @BeforeEach
    void setupDb() {
        repository.deleteAll().block();
    }

    @Test
    void contextLoads() {
    }

    @Test
    void getRecommendationsByProductId() {

        int productId = 1;

        sendCreateRecommendationEvent(productId, 1);
        sendCreateRecommendationEvent(productId, 2);
        sendCreateRecommendationEvent(productId, 3);

        assertEquals(3, repository.findByProductId(productId).count().block());

        getAndVerifyRecommendationsByProductId(productId)
            .jsonPath("$.length()").isEqualTo(3)
            .jsonPath("$[2].productId").isEqualTo(productId)
            .jsonPath("$[2].recommendationId").isEqualTo(3);
    }

    @Test
    void duplicateError() {

        int productId = 1;
        int recommendationId = 1;

        sendCreateRecommendationEvent(productId, recommendationId);

        assertEquals(1, repository.count().block());

        InvalidInputException thrown = assertThrows(
            InvalidInputException.class,
            () -> sendCreateRecommendationEvent(productId, recommendationId),
            "Expected a InvalidInputException here!");
        assertEquals("Duplicate key, Product Id: 1, Recommendation Id: 1", thrown.getMessage());

        assertEquals(1, repository.count().block());
    }

    @Test
    void deleteRecommendations() {

        int productId = 1;
        int recommendationId = 1;

        sendCreateRecommendationEvent(productId, recommendationId);;
        assertEquals(1, repository.findByProductId(productId).count().block());

        sendDeleteRecommendationEvent(productId);
        assertEquals(0, repository.findByProductId(productId).count().block());

        sendDeleteRecommendationEvent(productId);
    }

    @Test
    void getRecommendationsMissingParameter() {

        getAndVerifyRecommendationsByProductId("", HttpStatus.BAD_REQUEST)
            .jsonPath("$.path").isEqualTo("/recommendation")
            .jsonPath("$.message").isEqualTo("Required query parameter 'productId' is not present.");
    }

    @Test
    void getRecommendationsInvalidParameter() {

        getAndVerifyRecommendationsByProductId("?productId=no-integer", HttpStatus.BAD_REQUEST)
            .jsonPath("$.path").isEqualTo("/recommendation")
            .jsonPath("$.message").isEqualTo("Type mismatch.");
    }

    @Test
    void getRecommendationsNotFound() {

        getAndVerifyRecommendationsByProductId("?productId=113", HttpStatus.OK)
            .jsonPath("$.length()").isEqualTo(0);
    }

    @Test
    void getRecommendationsInvalidParameterNegativeValue() {

        int productIdInvalid = -1;

        getAndVerifyRecommendationsByProductId("?productId=" + productIdInvalid, HttpStatus.UNPROCESSABLE_ENTITY)
            .jsonPath("$.path").isEqualTo("/recommendation")
            .jsonPath("$.message").isEqualTo("Invalid productId: " + productIdInvalid);
    }

    private WebTestClient.BodyContentSpec getAndVerifyRecommendationsByProductId(int productId) {
        return getAndVerifyRecommendationsByProductId("?productId=" + productId, HttpStatus.OK);
    }

    private WebTestClient.BodyContentSpec getAndVerifyRecommendationsByProductId(String productIdQuery, HttpStatus expectedStatus) {
        return client.get()
            .uri("/recommendation" + productIdQuery)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(expectedStatus)
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody();
    }

    private void sendCreateRecommendationEvent(int productId, int recommendationId) {
        RecommendationRequest request = RecommendationRequest.of(
            productId,
            recommendationId,
            "Author " + recommendationId,
            recommendationId,
            "Content " + recommendationId,
            "SA"
        );
        Event<Integer, RecommendationRequest> event = Event.create(
            Type.CREATE,
            productId,
            request
        );
        messageProcessor.accept(event);
    }

    private void sendDeleteRecommendationEvent(int productId) {
        Event<Integer, RecommendationRequest> event = Event.create(
            Type.DELETE,
            productId,
            null
        );
        messageProcessor.accept(event);
    }
}