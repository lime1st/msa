package msa.lime1st.composite;

import static msa.lime1st.composite.IsSameEvent.sameEventExceptCreatedAt;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import msa.lime1st.api.core.product.ProductRequest;
import msa.lime1st.api.event.Event;
import msa.lime1st.api.event.Event.Type;
import org.junit.jupiter.api.Test;

public class IsSameEventTests {

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    void testEventObjectCompare() throws JsonProcessingException {

        // Event #1 and #2 are the same event, but occurs as different times
        // Event #3 and #4 are different events
        Event<Integer, ProductRequest> event1 = Event.create(
            Type.CREATE,
            1,
            ProductRequest.of(1, "name", 1, null));
        Event<Integer, ProductRequest> event2 = Event.create(
            Type.CREATE,
            1,
            ProductRequest.of(1, "name", 1, null));
        Event<Integer, ProductRequest> event3 = Event.create(
            Type.DELETE,
            1,
            null);
        Event<Integer, ProductRequest> event4 = Event.create(
            Type.CREATE,
            1,
            ProductRequest.of(2, "name", 1, null));

        String event1Json = mapper.writeValueAsString(event1);

        assertThat(event1Json, is(sameEventExceptCreatedAt(event2)));
        assertThat(event1Json, not(sameEventExceptCreatedAt(event3)));
        assertThat(event1Json, not(sameEventExceptCreatedAt(event4)));
    }
}
