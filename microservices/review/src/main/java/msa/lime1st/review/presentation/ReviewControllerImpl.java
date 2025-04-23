package msa.lime1st.review.presentation;

import java.util.List;
import java.util.logging.Level;
import msa.lime1st.api.core.review.ReviewApi;
import msa.lime1st.api.core.review.ReviewRequest;
import msa.lime1st.api.core.review.ReviewResponse;
import msa.lime1st.util.exception.InvalidInputException;
import msa.lime1st.review.infrastructure.persistence.ReviewEntity;
import msa.lime1st.review.infrastructure.persistence.ReviewRepository;
import msa.lime1st.util.http.ApiUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

@RestController
public class ReviewControllerImpl implements ReviewApi {

    private static final Logger LOG = LoggerFactory.getLogger(ReviewControllerImpl.class);

    private final ApiUtil apiUtil;
    private final Scheduler jdbcScheduler;
    private final ReviewMapper mapper;
    private final ReviewRepository repository;

    public ReviewControllerImpl(
        ApiUtil apiUtil,
        Scheduler jdbcScheduler,
        ReviewMapper mapper,
        ReviewRepository repository
    ) {
        this.apiUtil = apiUtil;
        this.jdbcScheduler = jdbcScheduler;
        this.mapper = mapper;
        this.repository = repository;
    }

    @Override
    public Mono<ReviewResponse> postReview(ReviewRequest request) {

        if (request.productId() < 1) {
            throw new InvalidInputException("Invalid productId: " + request.productId());
        }
        // review 는 product, recommendation 과 달리 mysql 을 사용하고, repository 가 reactive 가 아니다.
        // internalCreateReview(): 이전 블로킹코드 메소드를 Mono.fromCallable() 메서드를 사용해 Mono 객체로 감싸고 있다.
        // subscribeOn() 메서드를 사용해 jdbcScheduler 의 스레드 풀에서 블로킹 코드를 실행한다.
        // 즉 스레드 분기가 일어난다.
        return Mono.fromCallable(() -> internalCreateReview(request))
            .subscribeOn(jdbcScheduler);
    }

    @Override
    public Flux<ReviewResponse> getReviews(int productId) {

        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        LOG.info("Will get reviews for product with id={}", productId);

        return Mono.fromCallable(() -> internalGetReviews(productId))
            .flatMapMany(Flux::fromIterable)
            .log(LOG.getName(), Level.FINE)
            .subscribeOn(jdbcScheduler);
    }

    @Override
    public Mono<Void> deleteReviews(int productId) {

        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        return Mono.fromRunnable(() -> internalDeleteReviews(productId))
            .subscribeOn(jdbcScheduler)
            .then();
    }

    private ReviewResponse internalCreateReview(ReviewRequest request) {
        try {
            ReviewEntity entity = mapper.requestToEntity(request);
            ReviewEntity newEntity = repository.save(entity);

            LOG.debug(
                "createReview: created a review entity: {}/{}",
                request.productId(),
                request.reviewId());
            return mapper.entityToResponse(newEntity);
        } catch (DataIntegrityViolationException dive) {
            throw new InvalidInputException(
                "Duplicate key, Product Id: " + request.productId() +
                    ", Review Id: " + request.reviewId());
        }
    }

    private List<ReviewResponse> internalGetReviews(int productId) {

        List<ReviewEntity> entityList = repository.findByProductId(productId);
        List<ReviewResponse> responseList = mapper.entityListToResponseList(entityList);

        List<ReviewResponse> list = responseList.stream()
            .map(r -> r.withServiceAddress(apiUtil.getServiceAddress()))
            .toList();

        LOG.debug("/reviews response size: {}", list.size());

        return list;
    }

    private void internalDeleteReviews(int productId) {

        LOG.debug("deleteReviews: tries to delete reviews for the product with productId: {}",
            productId);

        repository.deleteAll(repository.findByProductId(productId));
    }
}