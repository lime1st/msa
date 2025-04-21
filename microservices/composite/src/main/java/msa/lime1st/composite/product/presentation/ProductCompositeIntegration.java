package msa.lime1st.composite.product.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import msa.lime1st.api.core.product.ProductApi;
import msa.lime1st.api.core.product.ProductRequest;
import msa.lime1st.api.core.product.ProductResponse;
import msa.lime1st.api.core.recommendation.RecommendationApi;
import msa.lime1st.api.core.recommendation.RecommendationRequest;
import msa.lime1st.api.core.recommendation.RecommendationResponse;
import msa.lime1st.api.core.review.ReviewApi;
import msa.lime1st.api.core.review.ReviewRequest;
import msa.lime1st.api.core.review.ReviewResponse;
import msa.lime1st.api.exception.InvalidInputException;
import msa.lime1st.api.exception.NotFoundException;
import msa.lime1st.util.http.HttpErrorInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class ProductCompositeIntegration implements ProductApi, RecommendationApi, ReviewApi {

    private static final Logger LOG = LoggerFactory.getLogger(ProductCompositeIntegration.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;

    private final String productServiceUrl;
    private final String recommendationServiceUrl;
    private final String reviewServiceUrl;

    public ProductCompositeIntegration(
        RestTemplate restTemplate,
        ObjectMapper mapper,
        @Value("${app.product.host}") String productServiceHost,
        @Value("${app.product.port}") int productServicePort,
        @Value("${app.recommendation.host}") String recommendationServiceHost,
        @Value("${app.recommendation.port}") int recommendationServicePort,
        @Value("${app.review.host}") String reviewServiceHost,
        @Value("${app.review.port}") int reviewServicePort
        ) {
        this.restTemplate = restTemplate;
        this.mapper = mapper;

        productServiceUrl = "http://" + productServiceHost + ":" + productServicePort + "/product";
        recommendationServiceUrl = "http://" + recommendationServiceHost + ":" + recommendationServicePort + "/recommendation";
        reviewServiceUrl = "http://" + reviewServiceHost + ":" + reviewServicePort + "/review";
    }

    @Override
    public ProductResponse postProduct(ProductRequest request) {
        try {
            String url = productServiceUrl;
            LOG.debug("Will post a new response to URL: {}", url);

            ProductResponse response = restTemplate.postForObject(
                url, request,
                ProductResponse.class
            );
            assert response != null;
            LOG.debug("Created a response with id: {}", response.productId());

            return response;

        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }

    }

    @Override
    public ProductResponse getProduct(int productId) {
        try {
            String url = productServiceUrl + "/" + productId;
            LOG.debug("Will call getProduct API on URL: {}", url);

            ProductResponse response = restTemplate.getForObject(url, ProductResponse.class);
            assert response != null;
            LOG.debug("Found a response with id: {}", response.productId());

            return response;

        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    @Override
    public void deleteProduct(int productId) {
        try {
            String url = productServiceUrl + "/" + productId;
            LOG.debug("Will call deleteProduct API on URL: {}", url);

            restTemplate.delete(url);
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    @Override
    public RecommendationResponse postRecommendation(RecommendationRequest request) {
        try {
            String url = recommendationServiceUrl;
            LOG.debug("Will call postRecommendation API on URL: {}", url);

            RecommendationResponse response = restTemplate.postForObject(
                url, request,
                RecommendationResponse.class
            );
            assert response != null;
            LOG.debug("Created a recommendation with id: {}", response.productId());

            return response;
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    @Override
    public List<RecommendationResponse> getRecommendations(int productId) {
        try {
            String url = recommendationServiceUrl + "?productId=" + productId;

            LOG.debug("Will call getRecommendations API on URL: {}", url);
            List<RecommendationResponse> recommendations = restTemplate
                .exchange(url, HttpMethod.GET, null,
                    new ParameterizedTypeReference<List<RecommendationResponse>>() {})
                .getBody();

            assert recommendations != null;
            LOG.debug("Found {} recommendations for a product with id: {}", recommendations.size(), productId);
            return recommendations;

        } catch (Exception ex) {
            LOG.warn("Got an exception while requesting recommendations, return zero recommendations: {}", ex.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public void deleteRecommendations(int productId) {
        try {
            String url = recommendationServiceUrl + "?productId=" + productId;
            LOG.debug("Will call deleteRecommendation API on URL: {}", url);

            restTemplate.delete(url);
        }catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    @Override
    public ReviewResponse postReview(ReviewRequest request) {
        try {
            String url = reviewServiceUrl;
            LOG.debug("Will call postReview API on URL: {}", url);

            ReviewResponse response = restTemplate.postForObject(
                url, request,
                ReviewResponse.class
            );
            assert response != null;
            LOG.debug("Created a review with id: {}", response.productId());

            return response;
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    @Override
    public List<ReviewResponse> getReviews(int productId) {
        try {
            String url = reviewServiceUrl + "?productId=" + productId;

            LOG.debug("Will call getReviews API on URL: {}", url);
            List<ReviewResponse> reviews = restTemplate
                .exchange(url, HttpMethod.GET, null,
                    new ParameterizedTypeReference<List<ReviewResponse>>() {})
                .getBody();

            assert reviews != null;
            LOG.debug("Found {} reviews for a product with id: {}", reviews.size(), productId);
            return reviews;
        } catch (Exception ex) {
            LOG.warn("Got an exception while requesting reviews, return zero reviews: {}", ex.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public void deleteReviews(int productId) {
        try {
            String url = reviewServiceUrl + "?productId=" + productId;
            LOG.debug("Will call deleteReview API on URL: {}", url);

            restTemplate.delete(url);
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    private RuntimeException handleHttpClientException(HttpClientErrorException ex) {
        HttpStatusCode statusCode = ex.getStatusCode();
        if (statusCode.equals(HttpStatus.NOT_FOUND)) {
            return new NotFoundException(getErrorMessage(ex));
        } else if (statusCode.equals(HttpStatus.UNPROCESSABLE_ENTITY)) {
            return new InvalidInputException(getErrorMessage(ex));
        }
        LOG.warn("Got an unexpected HTTP error: {}, will rethrow it", ex.getStatusCode());
        LOG.warn("Error body: {}", ex.getResponseBodyAsString());
        return ex;
    }

    private String getErrorMessage(HttpClientErrorException ex) {
        try {
            return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).message();
        } catch (IOException e) {
            return ex.getMessage();
        }
    }
}
