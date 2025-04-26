package msa.lime1st.product.presentation;

import javax.annotation.processing.Generated;
import msa.lime1st.api.core.product.ProductRequest;
import msa.lime1st.api.core.product.ProductResponse;
import msa.lime1st.product.infrastructure.persistence.ProductDocument;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-04-26T14:30:29+0900",
    comments = "version: 1.6.3, compiler: javac, environment: Java 17.0.14 (Eclipse Adoptium)"
)
@Component
public class ProductMapperImpl implements ProductMapper {

    @Override
    public ProductResponse documentToResponse(ProductDocument document) {
        if ( document == null ) {
            return null;
        }

        int productId = 0;
        String name = null;
        int weight = 0;

        productId = document.getProductId();
        name = document.getName();
        weight = document.getWeight();

        String serviceAddress = null;

        ProductResponse productResponse = new ProductResponse( productId, name, weight, serviceAddress );

        return productResponse;
    }

    @Override
    public ProductDocument requestToDocument(ProductRequest request) {
        if ( request == null ) {
            return null;
        }

        ProductDocument productDocument = new ProductDocument();

        if ( request.productId() != null ) {
            productDocument.setProductId( request.productId() );
        }
        productDocument.setName( request.name() );
        if ( request.weight() != null ) {
            productDocument.setWeight( request.weight() );
        }

        return productDocument;
    }
}
