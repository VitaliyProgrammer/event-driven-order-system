package com.orderline.order.mapper;

import com.orderline.order.dto.OutboxEventResponse;
import com.orderline.order.entity.OutboxEvent;
import org.mapstruct.Mapper;

@Mapper
public interface OutboxEventMapper {

    OutboxEventResponse toResponse(OutboxEvent event);
}
