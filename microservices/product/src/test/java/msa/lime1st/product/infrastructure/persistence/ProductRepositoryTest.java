package msa.lime1st.product.infrastructure.persistence;

import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import reactor.test.StepVerifier;

@DataMongoTest
class ProductRepositoryTest extends MongoDbTestBase{

    /*
    영속성 메서드가 Mono / Flux 를 반환하므로 테스트 메서드는 반환된 리액트브 객체에서 결과를 받을 때까지 기다려야한다.
    테스트 메서드는 Mono / Flux 객체의 block() 메서드를 직접호출해 결과를 받을 때까지 기다리거나 프로젝트 리액터의
    StepVerifier 헬퍼 클래스를 사용해 검증 가능한 비동기 이벤트 시퀀스를 선언한다.

    Product: StepVerifier 사용
    Recommendation: block() 메서드 사용
     */

    @Autowired
    private ProductRepository repository;

    private ProductDocument savedDocument;

    @BeforeEach
    void setupDb() {
        StepVerifier.create(repository.deleteAll()).verifyComplete();

        ProductDocument document = ProductDocument.create(
            1, "a", 1
        );

        StepVerifier.create(repository.save(document))
            .expectNextMatches(savedDocument -> {
                this.savedDocument = savedDocument;
                return areProductEqual(document, this.savedDocument);
            })
            .verifyComplete();
    }

    @Test
    void create() {

        ProductDocument document = ProductDocument.create(2, "n", 2);

        StepVerifier.create(repository.save(document))
            .expectNextMatches(savedDocument ->
                document.getProductId() == savedDocument.getProductId())
            .verifyComplete();

        StepVerifier.create(repository.findById(document.getId()))
            .expectNextMatches(foundDocument ->
                areProductEqual(document, foundDocument))
            .verifyComplete();

        StepVerifier.create(repository.count())
            .expectNext(2L)
            .verifyComplete();
    }

    @Test
    void update() {

        savedDocument.setName("n2");
        StepVerifier.create(repository.save(savedDocument))
            .expectNextMatches(updatedDocument ->
                updatedDocument.getName().equals("n2"))
            .verifyComplete();

        StepVerifier.create(repository.findById(savedDocument.getId()))
            .expectNextMatches(foundDocument ->
                foundDocument.getVersion() == 1 &&
                    foundDocument.getName().equals("n2"))
            .verifyComplete();
    }

    @Test
    void delete() {

        StepVerifier.create(repository.delete(savedDocument))
            .verifyComplete();
        StepVerifier.create(repository.existsById(savedDocument.getId()))
            .expectNext(false).verifyComplete();
    }

    @Test
    void getByProductId() {

        StepVerifier.create(repository.findByProductId(savedDocument.getProductId()))
            .expectNextMatches(foundEntity ->
                areProductEqual(savedDocument, foundEntity))
            .verifyComplete();
    }

    @Test
    void duplicateError() {

        ProductDocument entity = ProductDocument.create(
            savedDocument.getProductId(),
            "n",
            1);
        StepVerifier.create(repository.save(entity))
            .expectError(DuplicateKeyException.class)
            .verify();
    }

    @Test
    void optimisticLockError() {

        // 데이터베이스에서 가져온 엔티티를 변수 2개에 저장한다.
        ProductDocument document1 = repository.findById(savedDocument.getId()).block();
        ProductDocument document2 = repository.findById(savedDocument.getId()).block();

        // 첫 번째 엔티티 객체를 업데이트한다. -> document1 의 version 필드가 자동으로 증가된다.
        assert document1 != null;
        document1.setName("n1");
        repository.save(document1).block();

        // 두 번째 엔티티 객체를 업데이트한다.
        // 두 번째 엔티티 객체의 버전이 낮으므로 실패한다.
        // 즉 낙관적 잠금 오류가 발생해 실패한다.
        assert document2 != null;
        StepVerifier.create(repository.save(document2))
            .expectError(OptimisticLockingFailureException.class)
            .verify();

        // 데이터베이스에서 업데이트된 엔티티를 가져와서 새로운 값을 확인한다.
        StepVerifier.create(repository.findById(savedDocument.getId()))
            .expectNextMatches(foundDocument ->
                foundDocument.getVersion() == 1 &&
                    foundDocument.getName().equals("n1"))
            .verifyComplete();
    }

    private boolean areProductEqual(ProductDocument expectedEntity, ProductDocument actualEntity) {
        return
            (expectedEntity.getId().equals(actualEntity.getId()))
                && (Objects.equals(expectedEntity.getVersion(), actualEntity.getVersion()))
                && (expectedEntity.getProductId() == actualEntity.getProductId())
                && (expectedEntity.getName().equals(actualEntity.getName()))
                && (expectedEntity.getWeight() == actualEntity.getWeight());
    }
}