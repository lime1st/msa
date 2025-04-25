package msa.lime1st.product;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.HttpStatus.OK;

import java.util.function.Consumer;
import msa.lime1st.api.core.product.ProductRequest;
import msa.lime1st.api.event.Event;
import msa.lime1st.api.event.Event.Type;
import msa.lime1st.product.infrastructure.persistence.MongoDbTestBase;
import msa.lime1st.product.infrastructure.persistence.ProductRepository;
import msa.lime1st.util.exception.InvalidInputException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"eureka.client.enabled=false"}
)
class ProductApplicationTests extends MongoDbTestBase {

    @Autowired
    private WebTestClient client;

    @Autowired
    private ProductRepository repository;

    @Autowired
    private Consumer<Event<Integer, ProductRequest>> messageProcessor;

    @BeforeEach
    void setupDb() {
        repository.deleteAll().block();
    }

    @Test
    void contextLoads() {
    }

    @Test
    void getProductById() {

        int productId = 1;

        assertNull(repository.findByProductId(productId).block());
        assertEquals(0, repository.count().block());

        sendCreateProductEvent(productId);

        assertNotNull(repository.findByProductId(productId).block());
        assertEquals(1, repository.count().block());

        getAndVerifyProduct(productId, OK)
            .jsonPath("$.productId").isEqualTo(productId);
    }

    @Test
    void duplicateError() {

        int productId = 1;

        assertNull(repository.findByProductId(productId).block());

        sendCreateProductEvent(productId);

        assertNotNull(repository.findByProductId(productId).block());

        InvalidInputException thrown = assertThrows(
            InvalidInputException.class,
            () -> sendCreateProductEvent(productId),
            "Expected a InvalidInputException here!");
        assertEquals("Duplicate key, Product Id: " + productId, thrown.getMessage());
    }

    @Test
    void deleteProduct() {
        // product 도큐먼트를 삭제한 다음 다시 삭제 요청을 보내 멱등성(동일 리소스에 대한 응답은 같다)이 있는지 확인한다.
        // 즉 한 번 생성했던 도큐먼트에 대해서는 도큐먼트가 콜렉션에 존재하지 않더라도 OK 상태 코드를 반환해야 한다.

        int productId = 1;

        sendCreateProductEvent(productId);
        assertNotNull(repository.findByProductId(productId).block());

        sendDeleteProductEvent(productId);
        assertNull(repository.findByProductId(productId).block());

        sendDeleteProductEvent(productId);
    }

    @Test
    void getProductInvalidParameterString() {

        getAndVerifyProduct("/no-integer", HttpStatus.BAD_REQUEST)
            .jsonPath("$.path").isEqualTo("/product/no-integer")
            .jsonPath("$.message").isEqualTo("Type mismatch.");
    }

    @Test
    void getProductNotFound() {

        int productIdNotFound = 13;

        getAndVerifyProduct(productIdNotFound, HttpStatus.NOT_FOUND)
            .jsonPath("$.path").isEqualTo("/product/" + productIdNotFound)
            .jsonPath("$.message").isEqualTo("No product found for productId: " + productIdNotFound);
    }

    @Test
    void getProductInvalidParameterNegativeValue() {

        int productIdInvalid = -1;

        getAndVerifyProduct(productIdInvalid, HttpStatus.UNPROCESSABLE_ENTITY)
            .jsonPath("$.path").isEqualTo("/product/" + productIdInvalid)
            .jsonPath("$.message").isEqualTo("Invalid productId: " + productIdInvalid);
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

    private void sendCreateProductEvent(int productId) {
        ProductRequest product = ProductRequest.of(
            productId,
            "Name " + productId,
            productId,
            "SA"
        );
        Event<Integer, ProductRequest> event = Event.create(
            Type.CREATE,
            productId,
            product
        );
        // 테스트를 위해 메시징 시스템(rabbitmq or kafka)을 건너뛰고 메시지 프로세서를 직접 호출한다.
        messageProcessor.accept(event);
    }

    private void sendDeleteProductEvent(int productId) {
        Event<Integer, ProductRequest> event = Event.create(
            Type.DELETE,
            productId,
            null
        );
        messageProcessor.accept(event);
    }
}
