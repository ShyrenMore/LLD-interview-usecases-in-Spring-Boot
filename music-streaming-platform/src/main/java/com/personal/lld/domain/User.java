package com.personal.lld.domain;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private String id, username, email, name;
    private SubscriptionTier subscriptionTier;
    private long createdAt;
}
