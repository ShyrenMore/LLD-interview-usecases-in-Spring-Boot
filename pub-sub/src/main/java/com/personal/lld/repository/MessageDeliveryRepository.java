package com.personal.lld.repository;

import com.personal.lld.domain.DeliveryStatus;
import com.personal.lld.domain.MessageDelivery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@Slf4j
public class MessageDeliveryRepository {

    private final Map<String, MessageDelivery> deliveries =
        new ConcurrentHashMap<>();

    public MessageDelivery save(MessageDelivery delivery) {
        deliveries.put(delivery.getId(), delivery);
        return delivery;
    }

    public List<MessageDelivery> findPendingBySubscriber(
        String subscriberId
    ) {
        return deliveries.values()
            .stream()
            .filter(delivery ->
                subscriberId.equals(delivery.getSubscriberId())
                    && delivery.getStatus() == DeliveryStatus.PENDING
            )
            .toList();
    }

    public void updateDeliveryStatus(
        String deliveryId,
        DeliveryStatus status
    ) {
        MessageDelivery delivery = deliveries.get(deliveryId);

        if (delivery != null) {
            delivery.setStatus(status);

            if (status == DeliveryStatus.ACKNOWLEDGED) {
                delivery.setAcknowledgedAt(System.currentTimeMillis());
            }
        }
    }

    public void deleteById(String deliveryId) {
        deliveries.remove(deliveryId);
    }
}
