package msa.lime1st.api.core.review;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

public interface ReviewApi {

    @GetMapping("/review")
    List<ReviewResponse> getReviews(@RequestParam("productId") int productId);
}
