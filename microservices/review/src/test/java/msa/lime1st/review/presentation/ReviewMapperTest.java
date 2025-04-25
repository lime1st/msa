package msa.lime1st.review.presentation;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.List;
import msa.lime1st.api.core.review.ReviewRequest;
import msa.lime1st.api.core.review.ReviewResponse;
import msa.lime1st.review.infrastructure.persistence.ReviewEntity;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class ReviewMapperTest {

    private final ReviewMapper mapper = Mappers.getMapper(ReviewMapper.class);

    @Test
    void mapperTests() {

        assertNotNull(mapper);

        ReviewRequest request = ReviewRequest.of(1, 2, "a", "s", "c", "adr");

        ReviewEntity entity = mapper.requestToEntity(request);

        assertEquals(request.productId(), entity.getProductId());
        assertEquals(request.reviewId(), entity.getReviewId());
        assertEquals(request.author(), entity.getAuthor());
        assertEquals(request.subject(), entity.getSubject());
        assertEquals(request.content(), entity.getContent());

        ReviewResponse response = mapper.entityToResponse(entity);
        assertEquals(response.productId(), entity.getProductId());
        assertEquals(response.reviewId(), entity.getReviewId());
        assertEquals(response.author(), entity.getAuthor());
        assertEquals(response.subject(), entity.getSubject());
        assertEquals(response.content(), entity.getContent());
        assertNull(response.serviceAddress());
    }

    @Test
    void mapperListTests() {

        assertNotNull(mapper);

        ReviewRequest request = ReviewRequest.of(1, 2, "a", "s", "c", "adr");
        List<ReviewRequest> requestList = Collections.singletonList(request);

        List<ReviewEntity> entityList = mapper.requestListToEntityList(requestList);
        assertEquals(requestList.size(), entityList.size());

        ReviewEntity entity = entityList.get(0);

        assertEquals(request.productId(), entity.getProductId());
        assertEquals(request.reviewId(), entity.getReviewId());
        assertEquals(request.author(), entity.getAuthor());
        assertEquals(request.subject(), entity.getSubject());
        assertEquals(request.content(), entity.getContent());

        List<ReviewResponse> responseList = mapper.entityListToResponseList(entityList);
        assertEquals(requestList.size(), responseList.size());

        ReviewResponse response = responseList.get(0);

        assertEquals(response.productId(), entity.getProductId());
        assertEquals(response.reviewId(), entity.getReviewId());
        assertEquals(response.author(), entity.getAuthor());
        assertEquals(response.subject(), entity.getSubject());
        assertEquals(response.content(), entity.getContent());
        assertNull(response.serviceAddress());
    }
}