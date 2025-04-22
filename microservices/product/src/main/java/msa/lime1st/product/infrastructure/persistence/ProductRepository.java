package msa.lime1st.product.infrastructure.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface ProductRepository extends ReactiveCrudRepository<ProductDocument, String> {

    Mono<ProductDocument> findByProductId(int productId);
}
