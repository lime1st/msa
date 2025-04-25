package msa.lime1st.recommendation.infrastructure.persistence;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface RecommendationRepository extends
    ReactiveCrudRepository<RecommendationDocument, String> {

    Flux<RecommendationDocument> findByProductId(int productId);
}
