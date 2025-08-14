package com.org.mini_doodle.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = extractFieldErrors(ex.getBindingResult());

        ProblemDetail problemDetail = createProblemDetail(
                HttpStatus.BAD_REQUEST,
                "Validation Failed",
                "Request validation failed for one or more fields"
        );

        problemDetail.setProperty("fieldErrors", fieldErrors);
        return problemDetail;
    }

    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail handleNotFound(NotFoundException ex) {
        return createProblemDetail(
                HttpStatus.NOT_FOUND,
                "Not Found",
                ex.getMessage()
        );
    }

    @ExceptionHandler(OwnershipViolationException.class)
    public ProblemDetail handleOwnership(OwnershipViolationException ex) {
        return createProblemDetail(
                HttpStatus.FORBIDDEN,
                "Forbidden",
                "Access denied"
        );
    }

    @ExceptionHandler(OverlapConflictException.class)
    public ProblemDetail handleOverlap(OverlapConflictException ex) {
        return createProblemDetail(
                HttpStatus.CONFLICT,
                "Conflict",
                ex.getMessage()
        );
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ProblemDetail handleOptimisticLocking(OptimisticLockingFailureException ex) {
        return createProblemDetail(
                HttpStatus.CONFLICT,
                "Concurrent Modification",
                "Resource was modified by another request. Please retry."
        );
    }

    @ExceptionHandler(RuntimeException.class)
    public ProblemDetail handleGeneric(RuntimeException ex) {
        return createProblemDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Error",
                "An unexpected error occurred"
        );
    }

    // Clean helper method without type
    private ProblemDetail createProblemDetail(HttpStatus status, String title, String detail) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(status);
        problemDetail.setTitle(title);
        problemDetail.setDetail(detail);
        return problemDetail;
    }

    private Map<String, String> extractFieldErrors(BindingResult bindingResult) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError error : bindingResult.getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }
        return fieldErrors;
    }
}
