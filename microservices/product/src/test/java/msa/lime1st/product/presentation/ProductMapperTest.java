package msa.lime1st.product.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import msa.lime1st.api.core.product.ProductRequest;
import msa.lime1st.api.core.product.ProductResponse;
import msa.lime1st.product.infrastructure.persistence.ProductDocument;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class ProductMapperTest {

    private final ProductMapper mapper = Mappers.getMapper(ProductMapper.class);

    @Test
    void mapperTests() {

        assertNotNull(mapper);

        ProductRequest request = ProductRequest.of(1, "n", 1, "sa");

        ProductDocument document = mapper.requestToDocument(request);

        assertEquals(request.productId(), document.getProductId());
        assertEquals(request.name(), document.getName());
        assertEquals(request.weight(), document.getWeight());

        ProductResponse response = mapper.documentToResponse(document);

        assertEquals(request.productId(), response.productId());
        assertEquals(request.name(),      response.name());
        assertEquals(request.weight(),    response.weight());
        assertNull(response.serviceAddress());
    }
}