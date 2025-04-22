package msa.lime1st.api.event;

import java.time.ZonedDateTime;

public record Event<K, T> (
    Type eventType,
    K key,
    T data,
    ZonedDateTime eventCreatedAt
) {

    public enum  Type {
        CREATE,
        DELETE,
    }

    public static <K, T> Event<K, T> create(
        Type eventType,
        K key,
        T data
    ) {
        return new Event<>(
            eventType,
            key,
            data,
            ZonedDateTime.now()
        );
    }
}
