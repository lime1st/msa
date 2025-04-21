package msa.lime1st.product.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface ProductRepository extends MongoRepository<ProductDocument, String> {

    Optional<ProductDocument> findByProductId(int productId);
}
