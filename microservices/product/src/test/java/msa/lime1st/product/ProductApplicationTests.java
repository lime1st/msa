package msa.lime1st.product;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import msa.lime1st.api.core.product.ProductRequest;
import msa.lime1st.product.infrastructure.persistence.MongoDbTestBase;
import msa.lime1st.product.infrastructure.persistence.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductApplicationTests extends MongoDbTestBase {

    @Autowired
    private WebTestClient client;

    @Autowired
    private ProductRepository repository;

    @BeforeEach
    void setupDb() {
        repository.deleteAll();
    }

    @Test
    void contextLoads() {
    }

    @Test
    void getProductById() {

        int productId = 1;

        postAndVerifyProduct(productId, HttpStatus.OK);

        assertTrue(repository.findByProductId(productId)
            .isPresent());

        getAndVerifyProduct(productId, HttpStatus.OK)
            .jsonPath("$.productId")
            .isEqualTo(productId);
    }

    @Test
    void duplicateError() {

        int productId = 1;

        postAndVerifyProduct(productId, HttpStatus.OK);

        assertTrue(repository.findByProductId(productId)
            .isPresent());

        postAndVerifyProduct(productId, HttpStatus.UNPROCESSABLE_ENTITY)
            .jsonPath("$.path")
                .isEqualTo("/product")
            .jsonPath("$.message")
                .isEqualTo("Duplicate key, Product Id: " + productId);
    }

    @Test
    void deleteProduct() {
        // product 도큐먼트를 삭제한 다음 다시 삭제 요청을 보내 멱등성(동일 리소스에 대한 응답은 같다)이 있는지 확인한다.
        // 즉 한 번 생성했던 도큐먼트에 대해서는 도큐먼트가 콜렉션에 존재하지 않더라도 OK 상태 코드를 반환해야 한다.

        int productId = 1;

        postAndVerifyProduct(productId, HttpStatus.OK);
        assertTrue(repository.findByProductId(productId)
            .isPresent());

        deleteAndVerifyProduct(productId);
        assertFalse(repository.findByProductId(productId)
            .isPresent());

        deleteAndVerifyProduct(productId);
    }

    @Test
    void getProductInvalidParameterString() {

        getAndVerifyProduct("/no-integer", HttpStatus.BAD_REQUEST)
            .jsonPath("$.path")
                .isEqualTo("/product/no-integer")
            .jsonPath("$.message")
                .isEqualTo("Type mismatch.");
    }

    @Test
    void getProductNotFound() {

        int productIdNotFound = 13;

        getAndVerifyProduct(productIdNotFound, HttpStatus.NOT_FOUND)
            .jsonPath("$.path")
                .isEqualTo("/product/" + productIdNotFound)
            .jsonPath("$.message")
                .isEqualTo("No product found for productId: " + productIdNotFound);
    }

    @Test
    void getProductInvalidParameterNegativeValue() {

        int productIdInvalid = -1;

        getAndVerifyProduct(productIdInvalid, HttpStatus.UNPROCESSABLE_ENTITY)
            .jsonPath("$.path")
                .isEqualTo("/product/" + productIdInvalid)
            .jsonPath("$.message")
                .isEqualTo("Invalid productId: " + productIdInvalid);
    }

    private WebTestClient.BodyContentSpec getAndVerifyProduct(int productId, HttpStatus expectedStatus) {
        return getAndVerifyProduct("/" + productId, expectedStatus);
    }

    private WebTestClient.BodyContentSpec getAndVerifyProduct(String productIdPath, HttpStatus expectedStatus) {
        return client.get()
            .uri("/product" + productIdPath)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(expectedStatus)
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody();
    }

    private WebTestClient.BodyContentSpec postAndVerifyProduct(int productId, HttpStatus expectedStatus) {
        ProductRequest request = ProductRequest.of(
            productId,
            "Name " + productId,
            productId,
            "SA"
        );
        return client.post()
            .uri("/product")
            .body(Mono.just(request), ProductRequest.class)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(expectedStatus)
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody();
    }

    private void deleteAndVerifyProduct(int productId) {
        client.delete()
            .uri("/product/" + productId)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.OK)
            .expectBody();
    }
}
