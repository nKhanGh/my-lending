package com.nkhang.lending.assessment;

import com.nkhang.lending.assessment.dto.*;
import com.nkhang.lending.common.exception.AppException;
import com.nkhang.lending.common.exception.ErrorCode;
import com.nkhang.lending.config.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Clock;

@Service
@RequiredArgsConstructor
public class CreditProfileService {
    private final CreditProfileRepository profiles;
    private final CurrentUser currentUser;
    private final Clock clock;

    @Transactional
    public CreditProfileResponse save(CreditProfileRequest request) {
        var id = currentUser.id();
        var profile = profiles.findById(id).orElseGet(
                () -> new CreditProfile(id, request.onTimePayments(), request.missedPayments(), clock.instant()));
        profile.update(request.onTimePayments(), request.missedPayments(), clock.instant());
        return CreditProfileResponse.from(profiles.save(profile));
    }

    @Transactional(readOnly = true)
    public CreditProfileResponse get() {
        return profiles.findById(currentUser.id()).map(CreditProfileResponse::from)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Credit profile not found"));
    }
}
