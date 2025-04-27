package msa.lime1st.review.presentation;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import msa.lime1st.api.core.review.ReviewRequest;
import msa.lime1st.api.core.review.ReviewResponse;
import msa.lime1st.review.infrastructure.persistence.ReviewEntity;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-04-27T05:11:01+0900",
    comments = "version: 1.6.3, compiler: javac, environment: Java 17.0.14 (Eclipse Adoptium)"
)
@Component
public class ReviewMapperImpl implements ReviewMapper {

    @Override
    public ReviewResponse entityToResponse(ReviewEntity entity) {
        if ( entity == null ) {
            return null;
        }

        int productId = 0;
        int reviewId = 0;
        String author = null;
        String subject = null;
        String content = null;

        productId = entity.getProductId();
        reviewId = entity.getReviewId();
        author = entity.getAuthor();
        subject = entity.getSubject();
        content = entity.getContent();

        String serviceAddress = null;

        ReviewResponse reviewResponse = new ReviewResponse( productId, reviewId, author, subject, content, serviceAddress );

        return reviewResponse;
    }

    @Override
    public ReviewEntity requestToEntity(ReviewRequest request) {
        if ( request == null ) {
            return null;
        }

        ReviewEntity reviewEntity = new ReviewEntity();

        if ( request.productId() != null ) {
            reviewEntity.setProductId( request.productId() );
        }
        if ( request.reviewId() != null ) {
            reviewEntity.setReviewId( request.reviewId() );
        }
        reviewEntity.setAuthor( request.author() );
        reviewEntity.setSubject( request.subject() );
        reviewEntity.setContent( request.content() );

        return reviewEntity;
    }

    @Override
    public List<ReviewResponse> entityListToResponseList(List<ReviewEntity> entityList) {
        if ( entityList == null ) {
            return null;
        }

        List<ReviewResponse> list = new ArrayList<ReviewResponse>( entityList.size() );
        for ( ReviewEntity reviewEntity : entityList ) {
            list.add( entityToResponse( reviewEntity ) );
        }

        return list;
    }

    @Override
    public List<ReviewEntity> requestListToEntityList(List<ReviewRequest> requestList) {
        if ( requestList == null ) {
            return null;
        }

        List<ReviewEntity> list = new ArrayList<ReviewEntity>( requestList.size() );
        for ( ReviewRequest reviewRequest : requestList ) {
            list.add( requestToEntity( reviewRequest ) );
        }

        return list;
    }
}
