package msa.lime1st.composite;

import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.OK;

import java.util.Collections;
import msa.lime1st.api.composite.product.ProductAggregateResponse;
import msa.lime1st.api.composite.product.RecommendationSummary;
import msa.lime1st.api.composite.product.ReviewSummary;
import msa.lime1st.api.core.product.ProductResponse;
import msa.lime1st.api.core.recommendation.RecommendationResponse;
import msa.lime1st.api.core.review.ReviewResponse;
import msa.lime1st.api.exception.InvalidInputException;
import msa.lime1st.api.exception.NotFoundException;
import msa.lime1st.composite.product.presentation.ProductCompositeIntegration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CompositeApplicationTests {

    private static final int PRODUCT_ID_OK = 1;
    private static final int PRODUCT_ID_NOT_FOUND = 2;
    private static final int PRODUCT_ID_INVALID = 3;

    @Autowired
    private WebTestClient client;

    @MockitoBean
    private ProductCompositeIntegration compositeIntegration;

    @BeforeEach
    void setUp() {

        when(compositeIntegration.getProduct(PRODUCT_ID_OK))
            .thenReturn(ProductResponse.of(
                PRODUCT_ID_OK,
                "name", 1,
                "mock-address"
            ));
        when(compositeIntegration.getRecommendations(PRODUCT_ID_OK))
            .thenReturn(Collections.singletonList(RecommendationResponse.of(
                PRODUCT_ID_OK,
                1, "author", 1, "content",
                "mock address")
            ));
        when(compositeIntegration.getReviews(PRODUCT_ID_OK))
            .thenReturn(Collections.singletonList(ReviewResponse.of(
                PRODUCT_ID_OK,
                1, "author", "subject", "content",
                "mock address")
            ));

        when(compositeIntegration.getProduct(PRODUCT_ID_NOT_FOUND))
            .thenThrow(new NotFoundException("NOT FOUND: " + PRODUCT_ID_NOT_FOUND));

        when(compositeIntegration.getProduct(PRODUCT_ID_INVALID))
            .thenThrow(new InvalidInputException("INVALID: " + PRODUCT_ID_INVALID));
    }

    @Test
    void contextLoads() {
    }

    @Test
    void createCompositeProduct1() {

        ProductAggregateResponse response = ProductAggregateResponse.of(
            1, "name", 1,
            null, null, null
        );

        postAndVerifyProduct(response);
    }

    @Test
    void createCompositeProduct2() {
        ProductAggregateResponse response = ProductAggregateResponse.of(
            1, "name", 1,
            Collections.singletonList(RecommendationSummary.of(
                1, "a", 1, "c")),
            Collections.singletonList(ReviewSummary.of(
                1, "a", "s", "c")),
            null
        );

        postAndVerifyProduct(response);
    }

    @Test
    void deleteCompositeProduct() {
        ProductAggregateResponse response = ProductAggregateResponse.of(
            1, "name", 1,
            Collections.singletonList(RecommendationSummary.of(
                1, "a", 1, "c")),
            Collections.singletonList(ReviewSummary.of(
                1, "a", "s", "c")), null);

        postAndVerifyProduct(response);

        deleteAndVerifyProduct(response.productId());
        deleteAndVerifyProduct(response.productId());
    }

    @Test
    void getProductById() {

        getAndVerifyProduct(PRODUCT_ID_OK, OK)
            .jsonPath("$.productId")
                .isEqualTo(PRODUCT_ID_OK)
            .jsonPath("$.recommendations.length()")
                .isEqualTo(1)
            .jsonPath("$.reviews.length()")
                .isEqualTo(1);
    }

    @Test
    void getProductNotFound() {

        getAndVerifyProduct(PRODUCT_ID_NOT_FOUND, HttpStatus.NOT_FOUND)
            .jsonPath("$.path")
                .isEqualTo("/product-composite/" + PRODUCT_ID_NOT_FOUND)
            .jsonPath("$.message")
                .isEqualTo("NOT FOUND: " + PRODUCT_ID_NOT_FOUND);
    }

    @Test
    void getProductInvalidInput() {

        getAndVerifyProduct(PRODUCT_ID_INVALID, HttpStatus.UNPROCESSABLE_ENTITY)
            .jsonPath("$.path")
                .isEqualTo("/product-composite/" + PRODUCT_ID_INVALID)
            .jsonPath("$.message")
                .isEqualTo("INVALID: " + PRODUCT_ID_INVALID);
    }

    private WebTestClient.BodyContentSpec getAndVerifyProduct(int productId, HttpStatus expectedStatus) {
        return client.get()
            .uri("/product-composite/" + productId)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
                .isEqualTo(expectedStatus)
            .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
            .expectBody();
    }

    private void postAndVerifyProduct(ProductAggregateResponse compositeProduct) {
        client.post()
            .uri("/product-composite")
            .body(Mono.just(compositeProduct), ProductAggregateResponse.class)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.OK);
    }

    private void deleteAndVerifyProduct(int productId) {
        client.delete()
            .uri("/product-composite/" + productId)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.OK);
    }
}
