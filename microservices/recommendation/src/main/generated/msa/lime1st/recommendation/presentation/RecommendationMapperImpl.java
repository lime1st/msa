package msa.lime1st.recommendation.presentation;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import msa.lime1st.api.core.recommendation.RecommendationRequest;
import msa.lime1st.api.core.recommendation.RecommendationResponse;
import msa.lime1st.recommendation.infrastructure.persistence.RecommendationDocument;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-04-26T14:30:36+0900",
    comments = "version: 1.6.3, compiler: javac, environment: Java 17.0.14 (Eclipse Adoptium)"
)
@Component
public class RecommendationMapperImpl implements RecommendationMapper {

    @Override
    public RecommendationResponse documentToResponse(RecommendationDocument document) {
        if ( document == null ) {
            return null;
        }

        int rate = 0;
        int productId = 0;
        int recommendationId = 0;
        String author = null;
        String content = null;

        rate = document.getRating();
        productId = document.getProductId();
        recommendationId = document.getRecommendationId();
        author = document.getAuthor();
        content = document.getContent();

        String serviceAddress = null;

        RecommendationResponse recommendationResponse = new RecommendationResponse( productId, recommendationId, author, rate, content, serviceAddress );

        return recommendationResponse;
    }

    @Override
    public RecommendationDocument requestToDocument(RecommendationRequest request) {
        if ( request == null ) {
            return null;
        }

        RecommendationDocument recommendationDocument = new RecommendationDocument();

        if ( request.rate() != null ) {
            recommendationDocument.setRating( request.rate() );
        }
        if ( request.productId() != null ) {
            recommendationDocument.setProductId( request.productId() );
        }
        if ( request.recommendationId() != null ) {
            recommendationDocument.setRecommendationId( request.recommendationId() );
        }
        recommendationDocument.setAuthor( request.author() );
        recommendationDocument.setContent( request.content() );

        return recommendationDocument;
    }

    @Override
    public List<RecommendationResponse> documentListToResponseList(List<RecommendationDocument> documents) {
        if ( documents == null ) {
            return null;
        }

        List<RecommendationResponse> list = new ArrayList<RecommendationResponse>( documents.size() );
        for ( RecommendationDocument recommendationDocument : documents ) {
            list.add( documentToResponse( recommendationDocument ) );
        }

        return list;
    }

    @Override
    public List<RecommendationDocument> requestListToDocumentList(List<RecommendationRequest> requestList) {
        if ( requestList == null ) {
            return null;
        }

        List<RecommendationDocument> list = new ArrayList<RecommendationDocument>( requestList.size() );
        for ( RecommendationRequest recommendationRequest : requestList ) {
            list.add( requestToDocument( recommendationRequest ) );
        }

        return list;
    }
}
