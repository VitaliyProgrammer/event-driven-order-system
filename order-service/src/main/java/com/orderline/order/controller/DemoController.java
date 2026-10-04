package com.orderline.order.controller;

import com.orderline.order.dto.DemoEventResponse;
import com.orderline.order.service.DemoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/administration/demonstration")
@RequiredArgsConstructor
public class DemoController {

    private final DemoService demoService;

    @PostMapping("/duplicate-event")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public DemoEventResponse duplicateEvent(@RequestParam UUID orderId) {
        return demoService.duplicateLastEvent(orderId);
    }

    @PostMapping("/simulate-failure")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public DemoEventResponse simulateFailure() {
        return demoService.sendBrokenEvent();
    }
}
