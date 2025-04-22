package msa.lime1st.product.infrastructure.config;

import java.util.function.Consumer;
import msa.lime1st.api.core.product.ProductApi;
import msa.lime1st.api.core.product.ProductRequest;
import msa.lime1st.api.event.Event;
import msa.lime1st.api.exception.EventProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessageProcessorConfig {

  private static final Logger LOG = LoggerFactory.getLogger(MessageProcessorConfig.class);

  private final ProductApi api;

  public MessageProcessorConfig(ProductApi api) {
    this.api = api;
  }

  @Bean
  public Consumer<Event<Integer, ProductRequest>> messageProcessor() {
    return event -> {
      LOG.info("Process message created at {}...", event.eventCreatedAt());

      switch (event.eventType()) {

        case CREATE:
          ProductRequest request = event.data();
          LOG.info("Create request with ID: {}", request.productId());
          api.postProduct(request).block();
          break;

        case DELETE:
          int productId = event.key();
          LOG.info("Delete recommendations with ProductID: {}", productId);
          api.deleteProduct(productId).block();
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
