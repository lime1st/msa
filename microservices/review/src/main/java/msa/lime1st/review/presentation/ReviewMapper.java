package msa.lime1st.review.presentation;

import java.util.List;
import msa.lime1st.api.core.review.ReviewRequest;
import msa.lime1st.api.core.review.ReviewResponse;
import msa.lime1st.review.infrastructure.persistence.ReviewEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    @Mappings({
        @Mapping(target = "serviceAddress", ignore = true),
        @Mapping(target = "withServiceAddress", ignore = true)
    })
    ReviewResponse entityToResponse(ReviewEntity entity);

    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "version", ignore = true)
    })
    ReviewEntity requestToEntity(ReviewRequest request);

    List<ReviewResponse> entityListToResponseList(List<ReviewEntity> entityList);

    List<ReviewEntity> requestListToEntityList(List<ReviewRequest> requestList);
}
