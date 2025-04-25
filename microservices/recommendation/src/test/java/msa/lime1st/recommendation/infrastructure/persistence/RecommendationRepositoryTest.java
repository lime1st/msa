package msa.lime1st.recommendation.infrastructure.persistence;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;

@DataMongoTest
class RecommendationRepositoryTest extends MongoDbTestBase {

    @Autowired
    private RecommendationRepository repository;

    private RecommendationDocument savedDocument;

    @BeforeEach
    void setupDb() {
        repository.deleteAll().block();

        RecommendationDocument document = RecommendationDocument.create(
            1, 2, "a", 3, "c");
        savedDocument = repository.save(document)
            .block();

        assert savedDocument != null;
        assertEqualsRecommendation(document, savedDocument);
    }

    @Test
    void create() {

        RecommendationDocument document = RecommendationDocument.create(
            1, 3, "a", 3, "c");
        repository.save(document)
            .block();

        RecommendationDocument foundDocument = repository.findById(document.getId())
            .block();
        assert foundDocument != null;
        assertEqualsRecommendation(document, foundDocument);

        assertEquals(2, repository.count()
            .block());
    }

    @Test
    void update() {
        savedDocument.setAuthor("a2");
        repository.save(savedDocument)
            .block();

        RecommendationDocument foundDocument = repository.findById(savedDocument.getId())
            .block();
        assert foundDocument != null;
        assertEquals(1, foundDocument.getVersion());
        assertEquals("a2", foundDocument.getAuthor());
    }

    @Test
    void delete() {
        repository.delete(savedDocument).block();
        assertNotEquals(Boolean.TRUE, repository.existsById(savedDocument.getId())
            .block());
    }

    @Test
    void getByProductId() {
        List<RecommendationDocument> documentList = repository.findByProductId(savedDocument.getProductId())
            .collectList()
            .block();

        assertThat(documentList, hasSize(1));
        assertEqualsRecommendation(savedDocument, documentList.get(0));
    }

    @Test
    void duplicateError() {
        assertThrows(DuplicateKeyException.class, () -> {
            RecommendationDocument document = RecommendationDocument.create(
                1, 2, "a", 3, "c");
            repository.save(document)
                .block();
        });
    }

    @Test
    void optimisticLockError() {

        // Store the saved document in two separate document objects
        RecommendationDocument document1 = repository.findById(savedDocument.getId())
            .block();
        RecommendationDocument document2 = repository.findById(savedDocument.getId())
            .block();

        // Update the document using the first document object
        assert document1 != null;
        document1.setAuthor("a1");
        repository.save(document1).block();

        //  Update the document using the second document object.
        // This should fail since the second document now holds an old version number, i.e. an Optimistic Lock Error
        assertThrows(OptimisticLockingFailureException.class, () -> {
            assert document2 != null;
            document2.setAuthor("a2");
            repository.save(document2).block();
        });

        // Get the updated document from the database and verify its new update
        RecommendationDocument updatedDocument = repository.findById(savedDocument.getId())
            .block();
        assert updatedDocument != null;
        assertEquals(1, (int) updatedDocument.getVersion());
        assertEquals("a1", updatedDocument.getAuthor());
    }

    private void assertEqualsRecommendation(RecommendationDocument expectedEntity, RecommendationDocument actualEntity) {
        assertEquals(expectedEntity.getId(),               actualEntity.getId());
        assertEquals(expectedEntity.getVersion(),          actualEntity.getVersion());
        assertEquals(expectedEntity.getProductId(),        actualEntity.getProductId());
        assertEquals(expectedEntity.getRecommendationId(), actualEntity.getRecommendationId());
        assertEquals(expectedEntity.getAuthor(),           actualEntity.getAuthor());
        assertEquals(expectedEntity.getRating(),           actualEntity.getRating());
        assertEquals(expectedEntity.getContent(),          actualEntity.getContent());
    }
}