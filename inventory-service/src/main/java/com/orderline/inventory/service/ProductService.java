package com.orderline.inventory.service;

import com.orderline.inventory.dto.CreateProductRequest;
import com.orderline.inventory.dto.ProductResponse;
import com.orderline.inventory.entity.Product;
import com.orderline.inventory.exception.ProductNotFoundException;
import com.orderline.inventory.mapper.ProductMapper;
import com.orderline.inventory.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public Page<ProductResponse> findAll(Pageable pageable) {
        return productRepository.findAll(pageable).map(productMapper::toResponse);
    }

    public ProductResponse getById(UUID id) {
        return productRepository.findById(id)
                .map(productMapper::toResponse)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    @Transactional
    public ProductResponse create(CreateProductRequest request) {
        Product product = new Product(request.name(), request.price(), request.stock());
        return productMapper.toResponse(productRepository.saveAndFlush(product));
    }

    @Transactional
    public ProductResponse updateStock(UUID id, int stock) {
        Product product = productRepository.findForUpdate(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        product.changeStock(stock);
        return productMapper.toResponse(product);
    }
}
