package com.orderline.order.controller;

import com.orderline.order.dto.DeadLetterResponse;
import com.orderline.order.service.DeadLetterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/administration/dead-letters")
@RequiredArgsConstructor
public class DeadLetterController {

    private final DeadLetterService deadLetterService;

    @GetMapping
    public List<DeadLetterResponse> findAll() {
        return deadLetterService.findAll();
    }

    @PostMapping("/{topic}/{partition}/{offset}/retry")
    public DeadLetterResponse retry(@PathVariable String topic, @PathVariable int partition, @PathVariable long offset) {
        return deadLetterService.retry(topic, partition, offset);
    }
}
