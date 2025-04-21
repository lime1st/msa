package msa.lime1st.recommendation.infrastructure.persistence;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
        repository.deleteAll();

        RecommendationDocument document = RecommendationDocument.create(
            1, 2, "a", 3, "c");
        savedDocument = repository.save(document);

        assertEqualsRecommendation(document, savedDocument);
    }

    @Test
    void create() {

        RecommendationDocument document = RecommendationDocument.create(
            1, 3, "a", 3, "c");
        repository.save(document);

        RecommendationDocument foundDocument = repository.findById(document.getId()).get();
        assertEqualsRecommendation(document, foundDocument);

        assertEquals(2, repository.count());
    }

    @Test
    void update() {
        savedDocument.setAuthor("a2");
        repository.save(savedDocument);

        RecommendationDocument foundDocument = repository.findById(savedDocument.getId()).get();
        assertEquals(1, (long)foundDocument.getVersion());
        assertEquals("a2", foundDocument.getAuthor());
    }

    @Test
    void delete() {
        repository.delete(savedDocument);
        assertFalse(repository.existsById(savedDocument.getId()));
    }

    @Test
    void getByProductId() {
        List<RecommendationDocument> documentList = repository.findByProductId(savedDocument.getProductId());

        assertThat(documentList, hasSize(1));
        assertEqualsRecommendation(savedDocument, documentList.get(0));
    }

    @Test
    void duplicateError() {
        assertThrows(DuplicateKeyException.class, () -> {
            RecommendationDocument document = RecommendationDocument.create(
                1, 2, "a", 3, "c");
            repository.save(document);
        });
    }

    @Test
    void optimisticLockError() {

        // Store the saved document in two separate document objects
        RecommendationDocument document1 = repository.findById(savedDocument.getId()).get();
        RecommendationDocument document2 = repository.findById(savedDocument.getId()).get();

        // Update the document using the first document object
        document1.setAuthor("a1");
        repository.save(document1);

        //  Update the document using the second document object.
        // This should fail since the second document now holds an old version number, i.e. an Optimistic Lock Error
        assertThrows(OptimisticLockingFailureException.class, () -> {
            document2.setAuthor("a2");
            repository.save(document2);
        });

        // Get the updated document from the database and verify its new update
        RecommendationDocument updatedDocument = repository.findById(savedDocument.getId()).get();
        assertEquals(1, (int)updatedDocument.getVersion());
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