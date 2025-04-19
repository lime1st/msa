package msa.lime1st.review.presentation;

import java.util.ArrayList;
import java.util.List;
import msa.lime1st.api.core.review.ReviewApi;
import msa.lime1st.api.core.review.ReviewResponse;
import msa.lime1st.api.exception.InvalidInputException;
import msa.lime1st.util.http.ApiUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReviewControllerImpl implements ReviewApi {

    private static final Logger LOG = LoggerFactory.getLogger(ReviewControllerImpl.class);

    private final ApiUtil apiUtil;

    public ReviewControllerImpl(ApiUtil apiUtil) {
        this.apiUtil = apiUtil;
    }

    @Override
    public List<ReviewResponse> getReviews(int productId) {
        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        if (productId == 213) {
            LOG.debug("No reviews found for productId: {}", productId);
            return new ArrayList<>();
        }

        List<ReviewResponse> list = getReviewResponses(productId);

        LOG.debug("/reviews response size: {}", list.size());

        return list;
    }

    private List<ReviewResponse> getReviewResponses(int productId) {
        List<ReviewResponse> list = new ArrayList<>();
        list.add(new ReviewResponse(
            productId,
            1, "Author 1", "Subject 1", "Content 1",
            apiUtil.getServiceAddress()));
        list.add(new ReviewResponse(
            productId,
            2, "Author 2", "Subject 2", "Content 2",
            apiUtil.getServiceAddress()));
        list.add(new ReviewResponse(
            productId,
            3, "Author 3", "Subject 3", "Content 3",
            apiUtil.getServiceAddress()));
        return list;
    }
}
