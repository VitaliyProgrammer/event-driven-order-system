package com.orderline.order.controller;

import com.orderline.common.security.CurrentUser;
import com.orderline.order.dto.CreateOrderRequest;
import com.orderline.order.dto.OrderResponse;
import com.orderline.order.dto.StatusChangeResponse;
import com.orderline.order.entity.OrderStatus;
import com.orderline.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> create(@AuthenticationPrincipal Jwt jwt,
                                                @Valid @RequestBody CreateOrderRequest request) {
        OrderResponse created = orderService.create(CurrentUser.from(jwt).id(), request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{id}")
    public OrderResponse getById(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return orderService.getById(CurrentUser.from(jwt), id);
    }

    @GetMapping
    public PagedModel<OrderResponse> getMyOrders(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) @Nullable OrderStatus status,
            @ParameterObject
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return new PagedModel<>(orderService.getCustomerOrders(CurrentUser.from(jwt).id(), status, pageable));
    }

    @GetMapping("/{id}/timeline")
    public List<StatusChangeResponse> getTimeline(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return orderService.getTimeline(CurrentUser.from(jwt), id);
    }

    @PostMapping("/{id}/pay")
    public OrderResponse pay(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return orderService.pay(CurrentUser.from(jwt), id);
    }

    @PostMapping("/{id}/cancel")
    public OrderResponse cancel(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return orderService.cancel(CurrentUser.from(jwt), id);
    }

    @PostMapping("/{id}/ship")
    public OrderResponse ship(@PathVariable UUID id) {
        return orderService.ship(id);
    }

    @PostMapping("/{id}/deliver")
    public OrderResponse deliver(@PathVariable UUID id) {
        return orderService.deliver(id);
    }
}
