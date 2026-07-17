package com.ecommerce.ecommerce_backend.exception;

import com.ecommerce.ecommerce_backend.dto.ApiErrorResponse;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    GlobalExceptionHandler.class
            );

    @ExceptionHandler(
            MethodArgumentNotValidException.class
    )
    public ResponseEntity<ApiErrorResponse>
    handleValidationErrors(
            MethodArgumentNotValidException ex
    ) {

        Map<String, String> validationErrors =
                new LinkedHashMap<>();

        for (
                FieldError fieldError :
                ex.getBindingResult().getFieldErrors()
        ) {

            validationErrors.putIfAbsent(
                    fieldError.getField(),
                    fieldError.getDefaultMessage()
            );
        }

        return createResponse(
                HttpStatus.BAD_REQUEST,
                "Validation failed",
                validationErrors
        );
    }

    @ExceptionHandler(
            ConstraintViolationException.class
    )
    public ResponseEntity<ApiErrorResponse>
    handleConstraintViolation(
            ConstraintViolationException ex
    ) {

        Map<String, String> validationErrors =
                new LinkedHashMap<>();

        ex.getConstraintViolations()
                .forEach(violation -> {

                    String propertyPath =
                            violation.getPropertyPath()
                                    .toString();

                    String fieldName =
                            propertyPath.substring(
                                    propertyPath.lastIndexOf(
                                            '.'
                                    ) + 1
                            );

                    validationErrors.putIfAbsent(
                            fieldName,
                            violation.getMessage()
                    );
                });

        return createResponse(
                HttpStatus.BAD_REQUEST,
                "Validation failed",
                validationErrors
        );
    }

    @ExceptionHandler(
            HttpMessageNotReadableException.class
    )
    public ResponseEntity<ApiErrorResponse>
    handleMalformedJson(
            HttpMessageNotReadableException ex
    ) {

        return createResponse(
                HttpStatus.BAD_REQUEST,
                "Malformed JSON request"
        );
    }

    @ExceptionHandler(
            MethodArgumentTypeMismatchException.class
    )
    public ResponseEntity<ApiErrorResponse>
    handleArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex
    ) {

        return createResponse(
                HttpStatus.BAD_REQUEST,
                "Invalid value for parameter: "
                        + ex.getName()
        );
    }

    @ExceptionHandler(
            MissingServletRequestParameterException.class
    )
    public ResponseEntity<ApiErrorResponse>
    handleMissingParameter(
            MissingServletRequestParameterException ex
    ) {

        return createResponse(
                HttpStatus.BAD_REQUEST,
                "Missing required parameter: "
                        + ex.getParameterName()
        );
    }

    @ExceptionHandler(
            HttpRequestMethodNotSupportedException.class
    )
    public ResponseEntity<ApiErrorResponse>
    handleUnsupportedMethod(
            HttpRequestMethodNotSupportedException ex
    ) {

        return createResponse(
                HttpStatus.METHOD_NOT_ALLOWED,
                "HTTP method "
                        + ex.getMethod()
                        + " is not supported for this endpoint"
        );
    }

    @ExceptionHandler(
            HttpMediaTypeNotSupportedException.class
    )
    public ResponseEntity<ApiErrorResponse>
    handleUnsupportedMediaType(
            HttpMediaTypeNotSupportedException ex
    ) {

        return createResponse(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "Content type is not supported"
        );
    }

    @ExceptionHandler(
            NoResourceFoundException.class
    )
    public ResponseEntity<ApiErrorResponse>
    handleMissingResource(
            NoResourceFoundException ex
    ) {

        return createResponse(
                HttpStatus.NOT_FOUND,
                "Resource was not found"
        );
    }

    @ExceptionHandler(
            UserAlreadyExistException.class
    )
    public ResponseEntity<ApiErrorResponse>
    handleUserAlreadyExists(
            UserAlreadyExistException ex
    ) {

        return createResponse(
                HttpStatus.CONFLICT,
                "Username or email already exists"
        );
    }

    @ExceptionHandler(
            ResourceConflictException.class
    )
    public ResponseEntity<ApiErrorResponse>
    handleResourceConflict(
            ResourceConflictException ex
    ) {

        return createResponse(
                HttpStatus.CONFLICT,
                ex.getMessage()
        );
    }

    @ExceptionHandler(
            DataIntegrityViolationException.class
    )
    public ResponseEntity<ApiErrorResponse>
    handleDataIntegrityViolation(
            DataIntegrityViolationException ex
    ) {

        return createResponse(
                HttpStatus.CONFLICT,
                "A database constraint conflict occurred"
        );
    }

    @ExceptionHandler(
            OptimisticLockingFailureException.class
    )
    public ResponseEntity<ApiErrorResponse>
    handleOptimisticLockingFailure(
            OptimisticLockingFailureException ex
    ) {

        return createResponse(
                HttpStatus.CONFLICT,
                "Stock was updated by another request. Please retry."
        );
    }

    @ExceptionHandler(
            EmailFailureException.class
    )
    public ResponseEntity<ApiErrorResponse>
    handleEmailFailure(
            EmailFailureException ex
    ) {

        return createResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Failed to send email"
        );
    }

    @ExceptionHandler(
            UserNotVerifiedException.class
    )
    public ResponseEntity<ApiErrorResponse>
    handleUserNotVerified(
            UserNotVerifiedException ex
    ) {

        String message =
                ex.isNewEmailSend()
                        ? "User email is not verified. "
                          + "A new verification email has been sent."
                        : "User email is not verified.";

        return createResponse(
                HttpStatus.FORBIDDEN,
                message
        );
    }

    @ExceptionHandler(
            InvalidTokenException.class
    )
    public ResponseEntity<ApiErrorResponse>
    handleInvalidToken(
            InvalidTokenException ex
    ) {

        return createResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
    }

    @ExceptionHandler(
            ResourceNotFoundException.class
    )
    public ResponseEntity<ApiErrorResponse>
    handleResourceNotFound(
            ResourceNotFoundException ex
    ) {

        return createResponse(
                HttpStatus.NOT_FOUND,
                ex.getMessage()
        );
    }

    @ExceptionHandler(
            ForbiddenActionException.class
    )
    public ResponseEntity<ApiErrorResponse>
    handleForbiddenAction(
            ForbiddenActionException ex
    ) {

        return createResponse(
                HttpStatus.FORBIDDEN,
                ex.getMessage()
        );
    }

    @ExceptionHandler(
            InsufficientStockException.class
    )
    public ResponseEntity<ApiErrorResponse>
    handleInsufficientStock(
            InsufficientStockException ex
    ) {

        return createResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
    }

    @ExceptionHandler(
            InvalidOrderStatusException.class
    )
    public ResponseEntity<ApiErrorResponse>
    handleInvalidOrderStatus(
            InvalidOrderStatusException ex
    ) {

        return createResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
    }

    @ExceptionHandler(
            InvalidProductStatusException.class
    )
    public ResponseEntity<ApiErrorResponse>
    handleInvalidProductStatus(
            InvalidProductStatusException ex
    ) {

        return createResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
    }

    @ExceptionHandler(
            InvalidCredentialsException.class
    )
    public ResponseEntity<ApiErrorResponse>
    handleInvalidCredentials(
            InvalidCredentialsException ex
    ) {

        return createResponse(
                HttpStatus.UNAUTHORIZED,
                ex.getMessage()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse>
    handleUnexpectedException(
            Exception ex
    ) {

        LOGGER.error(
                "Unhandled application exception",
                ex
        );

        return createResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected server error occurred"
        );
    }

    private ResponseEntity<ApiErrorResponse>
    createResponse(
            HttpStatus status,
            String message
    ) {

        return createResponse(
                status,
                message,
                Map.of()
        );
    }

    private ResponseEntity<ApiErrorResponse>
    createResponse(
            HttpStatus status,
            String message,
            Map<String, String> validationErrors
    ) {

        ApiErrorResponse response =
                new ApiErrorResponse(
                        status.value(),
                        status.getReasonPhrase(),
                        message,
                        validationErrors
                );

        return ResponseEntity
                .status(status)
                .body(response);
    }
}