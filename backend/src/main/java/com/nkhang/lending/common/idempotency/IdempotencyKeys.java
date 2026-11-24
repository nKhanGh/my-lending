package com.nkhang.lending.common.idempotency;

import com.nkhang.lending.common.exception.AppException;
import com.nkhang.lending.common.exception.ErrorCode;

public final class IdempotencyKeys {
    private IdempotencyKeys() {}

    public static String requireValid(String key) {
        if (key == null || key.isBlank() || key.length() > 120 || !key.equals(key.trim())) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Idempotency-Key must contain 1 to 120 non-space-edge characters");
        }
        return key;
    }
}
