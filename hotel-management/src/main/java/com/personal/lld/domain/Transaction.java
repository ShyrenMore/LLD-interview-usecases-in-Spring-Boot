package com.personal.lld.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    private String id;
    private String bookingId;
    private long amountMinor;
    private String currency;
    private TransactionStatus status;
    private String providerRef;
    private long createdAt;
    private Long completedAt;
    private Long refundedAt;

    @Override
    public String toString() {
        return "Transaction{" +
                "id='" + id + '\'' +
                ", bookingId='" + bookingId + '\'' +
                ", amountMinor=" + amountMinor +
                ", status=" + status +
                '}';
    }
}
