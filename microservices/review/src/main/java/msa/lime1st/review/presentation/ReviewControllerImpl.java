package msa.lime1st.review.presentation;

import java.util.List;
import msa.lime1st.api.core.review.ReviewApi;
import msa.lime1st.api.core.review.ReviewRequest;
import msa.lime1st.api.core.review.ReviewResponse;
import msa.lime1st.api.exception.InvalidInputException;
import msa.lime1st.review.infrastructure.persistence.ReviewEntity;
import msa.lime1st.review.infrastructure.persistence.ReviewRepository;
import msa.lime1st.util.http.ApiUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReviewControllerImpl implements ReviewApi {

    private static final Logger LOG = LoggerFactory.getLogger(ReviewControllerImpl.class);

    private final ApiUtil apiUtil;
    private final ReviewMapper mapper;
    private final ReviewRepository repository;

    public ReviewControllerImpl(
        ApiUtil apiUtil,
        ReviewMapper mapper,
        ReviewRepository repository
    ) {
        this.apiUtil = apiUtil;
        this.mapper = mapper;
        this.repository = repository;
    }

    @Override
    public ReviewResponse postReview(ReviewRequest request) {
        try {
            ReviewEntity entity = mapper.requestToEntity(request);
            ReviewEntity savedEntity = repository.save(entity);

            LOG.debug("create review entity: {}/{}", request.productId(), request.reviewId());
            return mapper.entityToResponse(savedEntity);
        } catch (DataIntegrityViolationException dive) {
            throw new InvalidInputException("Duplicate key, Product Id: " + request.productId() +
                ", Review Id: " + request.reviewId());
        }
    }

    @Override
    public List<ReviewResponse> getReviews(int productId) {

        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        List<ReviewEntity> entityList = repository.findByProductId(productId);
        List<ReviewResponse> responseList = mapper.entityListToResponseList(entityList);

        List<ReviewResponse> list = responseList.stream()
            .map(r -> r.withServiceAddress(apiUtil.getServiceAddress()))
            .toList();

        LOG.debug("/reviews response size: {}", list.size());

        return list;
    }

    @Override
    public void deleteReviews(int productId) {
        LOG.debug("deleteReviews: tries to delete reviews for the product with productId: {}", productId);
        repository.deleteAll(repository.findByProductId(productId));
    }
}
