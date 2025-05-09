package msa.lime1st.api.core.review;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReviewApi {

    /**
     * Sample usage, see below.
     * <p>
     * curl -X POST $HOST:$PORT/review \ -H "Content-Type: application/json" --data \
     * '{"productId":123,"reviewId":456,"author":"me","subject":"yada, yada, yada","content":"yada,
     * yada, yada"}'
     *
     * @param request A JSON representation of the new review
     * @return A JSON representation of the newly created review
     */
    @PostMapping("/review")
    Mono<ReviewResponse> postReview(@RequestBody ReviewRequest request);

    /**
     * Sample usage: "curl $HOST:$PORT/review?productId=1".
     *
     * @param productId id of the product
     * @return the reviews of the product
     */
    @GetMapping("/review")
    Flux<ReviewResponse> getReviews(@RequestParam("productId") int productId);

    /**
     * Sample usage: "curl -X DELETE $HOST:$PORT/review?productId=1".
     *
     * @param productId id of the product
     */
    @DeleteMapping(value = "/review")
    Mono<Void> deleteReviews(@RequestParam(value = "productId", required = true) int productId);
}
