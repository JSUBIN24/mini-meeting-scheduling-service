package com.org.mini_doodle.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

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
        log.warn("Ownership violation: {}", ex.getMessage());
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

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex){
        return createProblemDetail(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                ex.getMessage()
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest req){
        log.warn("Invalid parameter: {}={} at {}", ex.getName(), ex.getValue(), req.getRequestURI());
        return createProblemDetail(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                ex.getMessage()
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException ex) {

        Map<String, String> violations = new LinkedHashMap<>();
        ex.getConstraintViolations().forEach(cv -> {
            String field = cv.getPropertyPath().toString();
            violations.put(field, cv.getMessage());
        });
        ProblemDetail problemDetail = createProblemDetail(
                HttpStatus.BAD_REQUEST,
                "Validation Failed",
                "One or more parameters are invalid"
        );
        problemDetail.setProperty("fieldErrors", violations);
        return problemDetail;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleUnreadable(HttpMessageNotReadableException ex, HttpServletRequest req) {
        String rootCause = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
        log.warn("Malformed JSON at {} -> {}", req.getRequestURI(), rootCause);
        return createProblemDetail(
                HttpStatus.BAD_REQUEST,
                "Malformed request body",
                rootCause
        );
    }


    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDuplicateKey(DataIntegrityViolationException ex) {
        log.error("Duplicate key error", ex);
        return createProblemDetail(
                HttpStatus.CONFLICT,
                "Duplicate Email",
                "A user with this email already exists"
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

/*    @ExceptionHandler(RuntimeException.class)
    public ProblemDetail handleGeneric(RuntimeException ex) {
        log.error("Unexpected error occurred", ex);
        return createProblemDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Error",
                "An unexpected error occurred"
        );
    }*/

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
