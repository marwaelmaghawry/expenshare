package xpenshare.exception;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import jakarta.inject.Singleton;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Produces
@Singleton
public class GlobalExceptionHandler implements ExceptionHandler<RuntimeException, HttpResponse<Map<String, Object>>> {

    @Override
    public HttpResponse<Map<String, Object>> handle(HttpRequest request, RuntimeException exception) {
        Map<String, Object> body = new HashMap<>();
        body.put("traceId", UUID.randomUUID().toString());

        if (exception instanceof NotFoundException) {
            body.put("error", exception.getMessage());
            body.put("code", "NOT_FOUND");
            return HttpResponse.notFound(body);
        }

        if (exception instanceof ConflictException) {
            body.put("error", exception.getMessage());
            body.put("code", "CONFLICT");
            return HttpResponse.status(HttpStatus.CONFLICT).body(body);
        }

        if (exception instanceof ValidationException ve) {
            body.put("error", ve.getMessage());
            body.put("code", "VALIDATION_ERROR");
            body.put("details", ve.getDetails());
            return HttpResponse.badRequest(body);
        }

        // fallback for other exceptions
        body.put("error", "Internal server error");
        body.put("code", "INTERNAL");
        return HttpResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
