package com.orderline.inventory.controller;

import com.orderline.inventory.dto.CreateProductRequest;
import com.orderline.inventory.dto.ProductResponse;
import com.orderline.inventory.dto.UpdateStockRequest;
import com.orderline.inventory.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/administration/products")
@RequiredArgsConstructor
public class ProductAdminController {

    private final ProductService productService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(@Valid @RequestBody CreateProductRequest request) {
        return productService.create(request);
    }

    @PutMapping("/{id}/stock")
    public ProductResponse updateStock(@PathVariable UUID id, @Valid @RequestBody UpdateStockRequest request) {
        return productService.updateStock(id, request.stock());
    }
}
