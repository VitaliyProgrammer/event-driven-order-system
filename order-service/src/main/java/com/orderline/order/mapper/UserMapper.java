package com.orderline.order.mapper;

import com.orderline.order.dto.UserResponse;
import com.orderline.order.entity.User;
import org.mapstruct.Mapper;

@Mapper
public interface UserMapper {

    UserResponse toResponse(User user);
}
