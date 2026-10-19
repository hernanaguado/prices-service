package com.hernanaguado.prices.infrastructure.adapter.in.rest;

import com.hernanaguado.prices.domain.exception.PriceNotFoundException;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.stream.Collectors;

/**
 * Translates exceptions into RFC 9457 problem details. Missing parameters and type mismatches are
 * already handled by {@link ResponseEntityExceptionHandler}.
 */
@RestControllerAdvice
class RestExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * The generated API interface is annotated with {@code @Validated}, so parameter constraints are
     * enforced by method validation and surface as this exception. The detail lists every violation as
     * {@code parameter: message}, without the Java method name that prefixes the property path.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    ProblemDetail handleConstraintViolation(ConstraintViolationException ex) {
        String detail = ex.getConstraintViolations().stream()
                .map(violation -> parameterName(violation.getPropertyPath()) + ": " + violation.getMessage())
                .sorted()
                .collect(Collectors.joining("; "));
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
    }

    @ExceptionHandler(PriceNotFoundException.class)
    ProblemDetail handlePriceNotFound(PriceNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail handleUnexpected(Exception ex) {
        logger.error("Unexpected error", ex);
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error");
    }

    private static String parameterName(Path propertyPath) {
        String name = null;
        for (Path.Node node : propertyPath) {
            name = node.getName();
        }
        return name;
    }
}
