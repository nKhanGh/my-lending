package com.nkhang.lending.common.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    @Builder.Default
    int code = 200;

    String message;
    T result;

    @Builder.Default
    Instant timestamp = Instant.now();

    public static <T> ApiResponse<T> success(T result) {
        return ApiResponse.<T>builder()
                .result(result)
                .message("Success")
                .build();
    }

    public static <T> ApiResponse<T> success(T result, String message) {
        return ApiResponse.<T>builder()
                .result(result)
                .message(message)
                .build();
    }
}

