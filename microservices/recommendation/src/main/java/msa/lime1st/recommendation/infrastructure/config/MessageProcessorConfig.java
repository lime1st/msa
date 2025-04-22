package msa.lime1st.recommendation.infrastructure.config;

import java.util.function.Consumer;
import msa.lime1st.api.core.recommendation.RecommendationApi;
import msa.lime1st.api.core.recommendation.RecommendationRequest;
import msa.lime1st.api.event.Event;
import msa.lime1st.api.exception.EventProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessageProcessorConfig {

  private static final Logger LOG = LoggerFactory.getLogger(MessageProcessorConfig.class);

  private final RecommendationApi api;

  public MessageProcessorConfig(RecommendationApi api) {
    this.api = api;
  }

  @Bean
  public Consumer<Event<Integer, RecommendationRequest>> messageProcessor() {
    return event -> {
      LOG.info("Process message created at {}...", event.eventCreatedAt());

      switch (event.eventType()) {

        case CREATE:
          RecommendationRequest request = event.data();
          LOG.info("Create request with ID: {}", request.productId());
          api.postRecommendation(request).block();
          break;

        case DELETE:
          int productId = event.key();
          LOG.info("Delete recommendations with ProductID: {}", productId);
          api.deleteRecommendations(productId).block();
          break;

        default:
          String errorMessage = "Incorrect event type: " + event.eventType() + ", expected a CREATE or DELETE event";
          LOG.warn(errorMessage);
          throw new EventProcessingException(errorMessage);
      }

      LOG.info("Message processing done!");

    };
  }
}
