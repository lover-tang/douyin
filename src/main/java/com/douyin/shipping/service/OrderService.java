package com.douyin.shipping.service;

import com.douyin.shipping.dto.*;
import com.douyin.shipping.entity.*;
import com.douyin.shipping.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final TrackingInfoRepository trackingInfoRepository;
    private final LabelConversionRepository labelConversionRepository;
    private final LogisticsMappingRepository logisticsMappingRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 创建订单
     */
    @Transactional
    public Order createOrder(OrderRequest request) {
        // 检查抖音订单号是否已存在
        if (orderRepository.findByDouyinOrderNo(request.getDouyinOrderNo()).isPresent()) {
            throw new RuntimeException("抖音订单号已存在: " + request.getDouyinOrderNo());
        }

        Order order = new Order();
        order.setDouyinOrderNo(request.getDouyinOrderNo());
        order.setTaobaoOrderNo(request.getTaobaoOrderNo());
        order.setProductName(request.getProductName());
        order.setReceiverName(request.getReceiverName());
        order.setReceiverPhone(request.getReceiverPhone());
        order.setReceiverProvince(request.getReceiverProvince());
        order.setReceiverCity(request.getReceiverCity());
        order.setReceiverDistrict(request.getReceiverDistrict());
        order.setReceiverAddress(request.getReceiverAddress());
        order.setRemark(request.getRemark());
        order.setStatus(0);

        return orderRepository.save(order);
    }

    /**
     * 更新订单
     */
    @Transactional
    public Order updateOrder(Long id, OrderRequest request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("订单不存在: " + id));

        order.setTaobaoOrderNo(request.getTaobaoOrderNo());
        order.setProductName(request.getProductName());
        order.setReceiverName(request.getReceiverName());
        order.setReceiverPhone(request.getReceiverPhone());
        order.setReceiverProvince(request.getReceiverProvince());
        order.setReceiverCity(request.getReceiverCity());
        order.setReceiverDistrict(request.getReceiverDistrict());
        order.setReceiverAddress(request.getReceiverAddress());
        order.setRemark(request.getRemark());

        return orderRepository.save(order);
    }

    /**
     * 删除订单
     */
    @Transactional
    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }

    /**
     * 查询订单详情
     */
    public Order getOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("订单不存在: " + id));
    }

    /**
     * 分页搜索订单
     */
    public Page<Order> searchOrders(String keyword, Integer status, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        return orderRepository.searchOrders(
                keyword != null && keyword.isEmpty() ? null : keyword,
                status,
                pageRequest
        );
    }

    /**
     * 获取仪表盘统计
     */
    public DashboardStats getDashboardStats() {
        DashboardStats stats = new DashboardStats();
        stats.setTotalOrders(orderRepository.count());
        stats.setPendingOrders(orderRepository.countByStatus(0) + orderRepository.countByStatus(1));
        stats.setShippedOrders(orderRepository.countByStatus(2));
        stats.setSyncingOrders(orderRepository.countByStatus(3));
        stats.setCompletedOrders(orderRepository.countByStatus(4));
        stats.setExceptionOrders(orderRepository.countByStatus(5));
        stats.setPendingLabelConvert(orderRepository.findByLabelConvertedFalseAndTaobaoTrackingNoIsNotNull().size());
        stats.setPendingTrackingSync(trackingInfoRepository.countBySyncedFalse());
        return stats;
    }
}
