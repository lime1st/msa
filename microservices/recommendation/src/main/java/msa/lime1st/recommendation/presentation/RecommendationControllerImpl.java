package msa.lime1st.recommendation.presentation;

import java.util.List;
import msa.lime1st.api.core.recommendation.RecommendationApi;
import msa.lime1st.api.core.recommendation.RecommendationRequest;
import msa.lime1st.api.core.recommendation.RecommendationResponse;
import msa.lime1st.api.exception.InvalidInputException;
import msa.lime1st.recommendation.infrastructure.persistence.RecommendationDocument;
import msa.lime1st.recommendation.infrastructure.persistence.RecommendationRepository;
import msa.lime1st.util.http.ApiUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RecommendationControllerImpl implements RecommendationApi {

    private static final Logger LOG = LoggerFactory.getLogger(RecommendationControllerImpl.class);

    private final ApiUtil apiUtil;
    private final RecommendationMapper mapper;
    private final RecommendationRepository repository;

    public RecommendationControllerImpl(ApiUtil apiUtil, RecommendationMapper mapper,
        RecommendationRepository repository) {
        this.apiUtil = apiUtil;
        this.mapper = mapper;
        this.repository = repository;
    }

    @Override
    public RecommendationResponse postRecommendation(RecommendationRequest request) {
        try {
            RecommendationDocument document = mapper.requestToDocument(request);
            RecommendationDocument saveDocument = repository.save(document);

            LOG.info("Saved recommendation: {}", document);
            return mapper.documentToResponse(saveDocument);
        } catch (DuplicateKeyException dke) {
            throw new InvalidInputException("Duplicate key, Product Id: " + request.productId() +
                ", Recommendation Id: " + request.recommendationId());
        }
    }

    @Override
    public List<RecommendationResponse> getRecommendations(int productId) {

        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        List<RecommendationDocument> documentList = repository.findByProductId(productId);
        List<RecommendationResponse> responseList = mapper.documentListToResponseList(documentList);

        List<RecommendationResponse> list = responseList.stream()
            .map(r -> r.withServiceAddress(apiUtil.getServiceAddress()))
            .toList();

        LOG.debug("/get recommendation response size: {}", list.size());

        return list;
    }

    @Override
    public void deleteRecommendations(int productId) {
        LOG.debug("/delete recommendations: tries to delete recommendations for the product with productId {}", productId);
        repository.deleteAll(repository.findByProductId(productId));
    }
}
