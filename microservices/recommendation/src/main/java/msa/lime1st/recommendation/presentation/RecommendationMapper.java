package msa.lime1st.recommendation.presentation;

import java.util.List;
import msa.lime1st.api.core.recommendation.RecommendationRequest;
import msa.lime1st.api.core.recommendation.RecommendationResponse;
import msa.lime1st.recommendation.infrastructure.persistence.RecommendationDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface RecommendationMapper {

    @Mappings({
        @Mapping(target = "rate", source = "rating"),
        @Mapping(target = "serviceAddress", ignore = true),
        @Mapping(target = "withServiceAddress", ignore = true)
    })
    RecommendationResponse documentToResponse(RecommendationDocument document);

    @Mappings({
        @Mapping(target = "rating", source = "rate"),
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "version", ignore = true)
    })
    RecommendationDocument requestToDocument(RecommendationRequest request);

    List<RecommendationResponse> documentListToResponseList(List<RecommendationDocument> documents);

    List<RecommendationDocument> requestListToDocumentList(List<RecommendationRequest> requestList);
}
