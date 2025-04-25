package msa.lime1st.recommendation.presentation;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.List;
import msa.lime1st.api.core.recommendation.RecommendationRequest;
import msa.lime1st.api.core.recommendation.RecommendationResponse;
import msa.lime1st.recommendation.infrastructure.persistence.RecommendationDocument;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class RecommendationMapperTest {

    private final RecommendationMapper mapper = Mappers.getMapper(RecommendationMapper.class);

    @Test
    void mapperTests() {

        assertNotNull(mapper);

        RecommendationRequest request = RecommendationRequest.of(1, 2, "a", 4, "C", "adr");

        RecommendationDocument document = mapper.requestToDocument(request);

        assertEquals(request.productId(), document.getProductId());
        assertEquals(request.recommendationId(), document.getRecommendationId());
        assertEquals(request.author(), document.getAuthor());
        assertEquals(request.rate(), document.getRating());
        assertEquals(request.content(), document.getContent());

        RecommendationResponse response = mapper.documentToResponse(document);

        assertEquals(request.productId(), response.productId());
        assertEquals(request.recommendationId(), response.recommendationId());
        assertEquals(request.author(), response.author());
        assertEquals(request.rate(), response.rate());
        assertEquals(request.content(), response.content());
        assertNull(response.serviceAddress());
    }

    @Test
    void mapperListTests() {

        assertNotNull(mapper);

        RecommendationRequest request = RecommendationRequest.of(1, 2, "a", 4, "C", "adr");
        List<RecommendationRequest> requestList = Collections.singletonList(request);

        List<RecommendationDocument> documentList = mapper.requestListToDocumentList(requestList);
        assertEquals(requestList.size(), documentList.size());

        RecommendationDocument document = documentList.get(0);

        assertEquals(request.productId(), document.getProductId());
        assertEquals(request.recommendationId(), document.getRecommendationId());
        assertEquals(request.author(), document.getAuthor());
        assertEquals(request.rate(), document.getRating());
        assertEquals(request.content(), document.getContent());

        List<RecommendationResponse> responseList = mapper.documentListToResponseList(documentList);
        assertEquals(requestList.size(), responseList.size());

        RecommendationResponse response = responseList.get(0);

        assertEquals(request.productId(), response.productId());
        assertEquals(request.recommendationId(), response.recommendationId());
        assertEquals(request.author(), response.author());
        assertEquals(request.rate(), response.rate());
        assertEquals(request.content(), response.content());
        assertNull(response.serviceAddress());
    }
}