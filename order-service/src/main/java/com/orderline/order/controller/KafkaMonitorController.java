package com.orderline.order.controller;

import com.orderline.order.dto.KafkaOverviewResponse;
import com.orderline.order.service.KafkaMonitorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/administration/kafka")
@RequiredArgsConstructor
public class KafkaMonitorController {

    private final KafkaMonitorService kafkaMonitorService;

    @GetMapping("/overview")
    public KafkaOverviewResponse getOverview() {
        return kafkaMonitorService.getOverview();
    }
}
