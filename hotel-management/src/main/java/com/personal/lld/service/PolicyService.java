package com.personal.lld.service;

import com.personal.lld.domain.Booking;
import com.personal.lld.domain.CancellationPolicy;
import com.personal.lld.domain.RefundDecision;
import org.springframework.stereotype.Service;

@Service
public class PolicyService {

    private static final long MILLIS_PER_HOUR = 60 * 60 * 1000L;

    public RefundDecision evaluateCancellation(
            Booking booking,
            CancellationPolicy policy,
            long nowUtc) {

        long millisUntilCheckIn = booking.getCheckInDateUtc() - nowUtc;

        if (millisUntilCheckIn < policy.getCutoffHoursBeforeCheckIn() * MILLIS_PER_HOUR) {
            return new RefundDecision(0, 0);
        }

        int refundPercent = policy.getRefundPercent();
        long refundAmount = booking.getTotalAmountMinor() * refundPercent / 100;

        return new RefundDecision(refundPercent, refundAmount);
    }
}
