package com.nkhang.lending.assessment.dto;
import com.nkhang.lending.assessment.CreditProfile;
import java.time.Instant;
public record CreditProfileResponse(int onTimePayments, int missedPayments, Instant updatedAt) {
    public static CreditProfileResponse from(CreditProfile value) {
        return new CreditProfileResponse(value.getOnTimePayments(), value.getMissedPayments(), value.getUpdatedAt());
    }
}
