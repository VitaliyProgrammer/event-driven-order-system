package com.orderline.order.scheduler;

import com.orderline.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
public class PaymentTimeoutScheduler {

    private final OrderService orderService;
    private final Duration paymentTimeout;

    public PaymentTimeoutScheduler(OrderService orderService,
                                   @Value("${orderline.orders.payment-timeout}") Duration paymentTimeout) {
        this.orderService = orderService;
        this.paymentTimeout = paymentTimeout;
    }

    @Scheduled(fixedDelayString = "${orderline.orders.timeout-check-interval}")
    public void cancelUnpaidOrders() {
        Instant reservedBefore = Instant.now().minus(paymentTimeout);
        for (UUID orderId : orderService.findUnpaidIds(reservedBefore)) {
            try {
                if (orderService.cancelUnpaid(orderId)) {
                    log.info("Order {} cancelled: not paid within {}", orderId, paymentTimeout);
                }
            } catch (ObjectOptimisticLockingFailureException e) {
                log.info("Order {} changed concurrently, skipping timeout cancellation", orderId);
            }
        }
    }
}
