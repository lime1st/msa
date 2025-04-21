package msa.lime1st.recommendation.infrastructure.persistence;

import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface RecommendationRepository extends CrudRepository<RecommendationDocument, String> {

    List<RecommendationDocument> findByProductId(int productId);
}
