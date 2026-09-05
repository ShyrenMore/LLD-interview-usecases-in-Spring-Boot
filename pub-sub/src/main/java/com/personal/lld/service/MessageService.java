package com.personal.lld.service;

import com.personal.lld.domain.DeliveryStatus;
import com.personal.lld.domain.MessageDelivery;
import com.personal.lld.repository.MessageDeliveryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageService {

    private final MessageDeliveryRepository messageDeliveryRepository;

    public void acknowledgeMessage(
        String messageId,
        String subscriberId
    ) {
        // TODO: Find the delivery record for messageId/subscriberId
        // and update its status to ACKNOWLEDGED.

        log.info(
            "Acknowledged message {} by subscriber {}",
            messageId,
            subscriberId
        );
    }
}
