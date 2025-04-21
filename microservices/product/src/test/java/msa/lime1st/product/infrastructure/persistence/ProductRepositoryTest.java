package msa.lime1st.product.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;

@DataMongoTest
class ProductRepositoryTest extends MongoDbTestBase{

    @Autowired
    private ProductRepository repository;

    private ProductDocument savedDocument;

    @BeforeEach
    void setupDb() {
        repository.deleteAll();

        ProductDocument document = ProductDocument.create(
            1, "a", 1
        );

        savedDocument = repository.save(document);

        assertEqualsProduct(document, savedDocument);
    }

    @Test
    void create() {

        ProductDocument newEntity = ProductDocument.create(2, "n", 2);
        repository.save(newEntity);

        ProductDocument foundEntity = repository.findById(newEntity.getId()).get();
        assertEqualsProduct(newEntity, foundEntity);

        assertEquals(2, repository.count());
    }

    @Test
    void update() {
        savedDocument.setName("n2");
        repository.save(savedDocument);

        ProductDocument foundEntity = repository.findById(savedDocument.getId()).get();
        assertEquals(1, (long)foundEntity.getVersion());
        assertEquals("n2", foundEntity.getName());
    }

    @Test
    void delete() {
        repository.delete(savedDocument);
        assertFalse(repository.existsById(savedDocument.getId()));
    }

    @Test
    void getByProductId() {
        Optional<ProductDocument> entity = repository.findByProductId(savedDocument.getProductId());

        assertTrue(entity.isPresent());
        assertEqualsProduct(savedDocument, entity.get());
    }

    @Test
    void duplicateError() {
        assertThrows(DuplicateKeyException.class, () -> {
            ProductDocument entity =ProductDocument.create(savedDocument.getProductId(), "n", 1);
            repository.save(entity);
        });
    }

    @Test
    void optimisticLockError() {

        // 데이터베이스에서 가져온 엔티티를 변수 2개에 저장한다.
        ProductDocument document1 = repository.findById(savedDocument.getId()).get();
        ProductDocument document2 = repository.findById(savedDocument.getId()).get();

        // 첫 번째 엔티티 객체를 업데이트한다. -> document1 의 version 필드가 자동으로 증가된다.
        document1.setName("n1");
        repository.save(document1);

        // 두 번째 엔티티 객체를 업데이트한다.
        // 두 번째 엔티티 객체의 버전이 낮으므로 실패한다.
        // 즉 낙관적 잠금 오류가 발생해 실패한다.
        assertThrows(OptimisticLockingFailureException.class, () -> {
            document2.setName("n2");
            repository.save(document2);
        });

        // 데이터베이스에서 업데이트된 엔티티를 가져와서 새로운 값을 확인한다.
        ProductDocument updatedEntity = repository.findById(savedDocument.getId()).get();
        assertEquals(1, (int)updatedEntity.getVersion());
        assertEquals("n1", updatedEntity.getName());
    }

    @Test
    void paging() {

        repository.deleteAll();

        List<ProductDocument> newProducts = IntStream.rangeClosed(1001, 1010)
            .mapToObj(i -> ProductDocument.create(i, "name " + i, i))
            .collect(Collectors.toList());
        repository.saveAll(newProducts);

        Pageable nextPage = PageRequest.of(
            0, 4,
            Direction.ASC, "productId");
        nextPage = testNextPage(
            nextPage,
            "[1001, 1002, 1003, 1004]",
            true);
        nextPage = testNextPage(
            nextPage,
            "[1005, 1006, 1007, 1008]",
            true);
        nextPage = testNextPage(
            nextPage,
            "[1009, 1010]",
            false);
    }

    private Pageable testNextPage(
        Pageable nextPage,
        String expectedProductIds,
        boolean expectsNextPage
    ) {
        Page<ProductDocument> productPage = repository.findAll(nextPage);
        assertEquals(expectedProductIds, productPage.getContent().stream()
            .map(ProductDocument::getProductId)
            .toList()
            .toString());
        assertEquals(expectsNextPage, productPage.hasNext());
        return productPage.nextPageable();
    }

    private void assertEqualsProduct(ProductDocument expectedEntity, ProductDocument actualEntity) {
        assertEquals(expectedEntity.getId(),               actualEntity.getId());
        assertEquals(expectedEntity.getVersion(),          actualEntity.getVersion());
        assertEquals(expectedEntity.getProductId(),        actualEntity.getProductId());
        assertEquals(expectedEntity.getName(),           actualEntity.getName());
        assertEquals(expectedEntity.getWeight(),           actualEntity.getWeight());
    }
}