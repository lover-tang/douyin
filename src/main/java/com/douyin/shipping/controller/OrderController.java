package com.douyin.shipping.controller;

import com.douyin.shipping.dto.ApiResult;
import com.douyin.shipping.dto.DashboardStats;
import com.douyin.shipping.dto.OrderRequest;
import com.douyin.shipping.entity.Order;
import com.douyin.shipping.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ApiResult<Order> createOrder(@Validated @RequestBody OrderRequest request) {
        try {
            return ApiResult.success(orderService.createOrder(request));
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ApiResult<Order> updateOrder(@PathVariable Long id,
                                        @Validated @RequestBody OrderRequest request) {
        try {
            return ApiResult.success(orderService.updateOrder(id, request));
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ApiResult<Void> deleteOrder(@PathVariable Long id) {
        try {
            orderService.deleteOrder(id);
            return ApiResult.success();
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ApiResult<Order> getOrder(@PathVariable Long id) {
        try {
            return ApiResult.success(orderService.getOrder(id));
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }

    @GetMapping
    public ApiResult<Page<Order>> searchOrders(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResult.success(orderService.searchOrders(keyword, status, page, size));
    }

    @GetMapping("/dashboard")
    public ApiResult<DashboardStats> getDashboardStats() {
        return ApiResult.success(orderService.getDashboardStats());
    }
}
