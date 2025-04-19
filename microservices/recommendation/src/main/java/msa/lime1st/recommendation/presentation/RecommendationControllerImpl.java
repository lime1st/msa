package msa.lime1st.recommendation.presentation;

import java.util.ArrayList;
import java.util.List;
import msa.lime1st.api.core.recommendation.RecommendationApi;
import msa.lime1st.api.core.recommendation.RecommendationResponse;
import msa.lime1st.api.exception.InvalidInputException;
import msa.lime1st.util.http.ApiUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RecommendationControllerImpl implements RecommendationApi {

    private static final Logger LOG = LoggerFactory.getLogger(RecommendationControllerImpl.class);

    private final ApiUtil apiUtil;

    public RecommendationControllerImpl(ApiUtil apiUtil) {
        this.apiUtil = apiUtil;
    }

    @Override
    public List<RecommendationResponse> getRecommendations(int productId) {
        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        if (productId == 113) {
            LOG.debug("No recommendations found for productId: {}", productId);
            return new ArrayList<>();
        }

        List<RecommendationResponse> list = getRecommendationResponses(productId);

        LOG.debug("/recommendation response size: {}", list.size());

        return list;
    }

    private List<RecommendationResponse> getRecommendationResponses(int productId) {
        List<RecommendationResponse> list = new ArrayList<>();
        list.add(new RecommendationResponse(
            productId,
            1, "Author 1", 1, "Content 1",
            apiUtil.getServiceAddress())
        );
        list.add(new RecommendationResponse(
            productId,
            2, "Author 2", 2, "Content 2",
            apiUtil.getServiceAddress())
        );
        list.add(new RecommendationResponse(
            productId,
            3, "Author 3", 3, "Content 3",
            apiUtil.getServiceAddress())
        );
        return list;
    }
}
