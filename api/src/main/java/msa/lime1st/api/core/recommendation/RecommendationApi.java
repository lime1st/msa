package msa.lime1st.api.core.recommendation;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

public interface RecommendationApi {

    @GetMapping("/recommendation")
    List<RecommendationResponse> getRecommendations(@RequestParam("productId") int productId);
}
