package msa.lime1st.util.http;

import msa.lime1st.util.exception.BadRequestException;
import msa.lime1st.util.exception.InvalidInputException;
import msa.lime1st.util.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalControllerException {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalControllerException.class);

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BadRequestException.class)
    public HttpErrorInfo handleBadRequestException(
        ServerHttpRequest request,
        BadRequestException ex
    ) {
        return createHttpErrorInfo(
            HttpStatus.BAD_REQUEST,
            request,
            ex
        );
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    public HttpErrorInfo handleNotFoundExceptions(
        ServerHttpRequest request,
        NotFoundException ex
    ) {

        return createHttpErrorInfo(
            HttpStatus.NOT_FOUND,
            request,
            ex
        );
    }

    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    @ExceptionHandler(InvalidInputException.class)
    public HttpErrorInfo handleInvalidInputException(
        ServerHttpRequest request,
        InvalidInputException ex
    ) {

        return createHttpErrorInfo(
            HttpStatus.UNPROCESSABLE_ENTITY,
            request,
            ex
        );
    }

    private HttpErrorInfo createHttpErrorInfo(
        HttpStatus httpStatus,
        ServerHttpRequest request,
        Exception ex
    ) {

        final String path = request.getPath()
            .pathWithinApplication()
            .value();
        final String message = ex.getMessage();

        LOG.debug("Returning HTTP status: {} for path: {}, message: {}", httpStatus, path, message);
        return HttpErrorInfo.of(
            httpStatus,
            path,
            message
        );
    }
}
