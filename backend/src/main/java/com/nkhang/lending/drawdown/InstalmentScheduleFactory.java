package com.nkhang.lending.drawdown;

import com.nkhang.lending.common.exception.AppException;
import com.nkhang.lending.common.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class InstalmentScheduleFactory {
    public List<Instalment> create(UUID drawdownId, BigDecimal amount, LocalDate drawdownDate) {
        if (amount.compareTo(new BigDecimal("0.04")) < 0 || amount.scale() > 2) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Drawdown must be at least SGD 0.04 with two decimal places");
        }
        var regular = amount.divide(new BigDecimal("4"), 2, RoundingMode.DOWN);
        var last = amount.subtract(regular.multiply(new BigDecimal("3")));
        var instalments = new ArrayList<Instalment>(4);
        for (int sequence = 1; sequence <= 4; sequence++) {
            instalments.add(new Instalment(drawdownId, sequence, sequence == 4 ? last : regular,
                    drawdownDate.plusMonths(sequence)));
        }
        return instalments;
    }
}
