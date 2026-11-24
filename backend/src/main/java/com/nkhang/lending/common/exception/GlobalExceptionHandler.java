package com.nkhang.lending.common.exception;

import com.nkhang.lending.common.api.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    ResponseEntity<ApiResponse<Void>> handleAppException(AppException exception) {
        ErrorCode error = exception.getErrorCode();
        return ResponseEntity.status(error.getHttpStatus())
                .body(errorResponse(error, exception.getMessage()));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class})
    ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest().body(errorResponse(ErrorCode.INVALID_REQUEST, message));
    }

    @ExceptionHandler({MissingRequestHeaderException.class, HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class})
    ResponseEntity<ApiResponse<Void>> handleMalformedRequest(Exception exception) {
        return ResponseEntity.badRequest()
                .body(errorResponse(ErrorCode.INVALID_REQUEST, exception.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception exception) {
        log.error("Unhandled application error", exception);
        ErrorCode error = ErrorCode.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(error.getHttpStatus()).body(errorResponse(error, error.getMessage()));
    }

    private ApiResponse<Void> errorResponse(ErrorCode error, String message) {
        return ApiResponse.<Void>builder()
                .code(error.getCode())
                .message(message)
                .build();
    }
}

