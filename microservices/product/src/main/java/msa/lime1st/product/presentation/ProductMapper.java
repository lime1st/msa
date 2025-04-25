package msa.lime1st.product.presentation;

import msa.lime1st.api.core.product.ProductRequest;
import msa.lime1st.api.core.product.ProductResponse;
import msa.lime1st.product.infrastructure.persistence.ProductDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mappings({
        @Mapping(target = "serviceAddress", ignore = true),
        @Mapping(target = "withServiceAddress", ignore = true)
    })
    ProductResponse documentToResponse(ProductDocument document);

    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "version", ignore = true)
    })
    ProductDocument requestToDocument(ProductRequest request);

}
