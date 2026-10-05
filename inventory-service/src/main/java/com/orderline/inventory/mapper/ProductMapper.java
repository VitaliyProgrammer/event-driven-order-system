package com.orderline.inventory.mapper;

import com.orderline.inventory.dto.ProductResponse;
import com.orderline.inventory.entity.Product;
import org.mapstruct.Mapper;

@Mapper
public interface ProductMapper {

    ProductResponse toResponse(Product product);
}
