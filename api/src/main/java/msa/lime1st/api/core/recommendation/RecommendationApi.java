package msa.lime1st.api.core.recommendation;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RecommendationApi {

    /**
     * Sample usage, see below.
     *
     * curl -X POST $HOST:$PORT/recommendation \
     *   -H "Content-Type: application/json" --data \
     *   '{"productId":123,"recommendationId":456,"author":"me","rate":5,"content":"yada, yada, yada"}'
     *
     * @param request A JSON representation of the new recommendation
     * @return A JSON representation of the newly created recommendation
     */
    @PostMapping("/recommendation")
    Mono<RecommendationResponse> postRecommendation(@RequestBody RecommendationRequest request);

    /**
     * Sample usage: "curl $HOST:$PORT/recommendation?productId=1".
     *
     * @param productId id of the product
     * @return the recommendations of the product
     */
    @GetMapping("/recommendation")
    Flux<RecommendationResponse> getRecommendations(@RequestParam("productId") int productId);

    /**
     * Sample usage: "curl -X DELETE $HOST:$PORT/recommendation?productId=1".
     *
     * @param productId id of the product
     */
    @DeleteMapping("/recommendation")
    Mono<Void> deleteRecommendations(@RequestParam("productId") int productId);
}
