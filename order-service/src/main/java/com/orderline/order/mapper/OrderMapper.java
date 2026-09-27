package com.orderline.order.mapper;

import com.orderline.order.dto.OrderItemResponse;
import com.orderline.order.dto.OrderResponse;
import com.orderline.order.entity.Order;
import com.orderline.order.entity.OrderItem;
import org.mapstruct.Mapper;

@Mapper
public interface OrderMapper {

    OrderResponse toResponse(Order order);

    OrderItemResponse toItemResponse(OrderItem item);
}
