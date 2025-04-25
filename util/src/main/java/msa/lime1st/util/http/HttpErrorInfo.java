package msa.lime1st.util.http;

import java.time.ZonedDateTime;
import org.springframework.http.HttpStatus;

public record HttpErrorInfo (
    ZonedDateTime timestamp,
    String path,
    HttpStatus httpStatus,
    String message
) {

    public static HttpErrorInfo of(
        HttpStatus httpStatus,
        String path,
        String message
    ) {
        return new HttpErrorInfo(
            ZonedDateTime.now(),
            path,
            httpStatus,
            message
        );
    }
}
