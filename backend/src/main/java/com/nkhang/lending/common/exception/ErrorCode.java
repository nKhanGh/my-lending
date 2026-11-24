package com.nkhang.lending.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    INVALID_REQUEST(40001, "Invalid request", HttpStatus.BAD_REQUEST),
    RESOURCE_NOT_FOUND(40401, "Resource not found", HttpStatus.NOT_FOUND),
    APPLICATION_ALREADY_APPROVED(40901, "Demo user already has a credit account", HttpStatus.CONFLICT),
    INSUFFICIENT_CREDIT(40902, "Insufficient available credit", HttpStatus.CONFLICT),
    INVALID_REPAYMENT_STATE(40903, "Instalment cannot be repaid in this state or order", HttpStatus.CONFLICT),
    IDEMPOTENCY_KEY_REUSED(40904, "Idempotency key was already used with a different request", HttpStatus.CONFLICT),
    INTERNAL_SERVER_ERROR(50000, "An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;
}
