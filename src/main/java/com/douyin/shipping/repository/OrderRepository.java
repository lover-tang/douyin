package com.douyin.shipping.repository;

import com.douyin.shipping.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByDouyinOrderNo(String douyinOrderNo);

    Optional<Order> findByTaobaoTrackingNo(String taobaoTrackingNo);

    List<Order> findByStatus(Integer status);

    List<Order> findByTrackingSyncEnabledTrue();

    @Query("SELECT o FROM Order o WHERE " +
           "(:keyword IS NULL OR o.douyinOrderNo LIKE %:keyword% " +
           "OR o.taobaoOrderNo LIKE %:keyword% " +
           "OR o.receiverName LIKE %:keyword% " +
           "OR o.taobaoTrackingNo LIKE %:keyword% " +
           "OR o.douyinTrackingNo LIKE %:keyword%) " +
           "AND (:status IS NULL OR o.status = :status)")
    Page<Order> searchOrders(@Param("keyword") String keyword,
                             @Param("status") Integer status,
                             Pageable pageable);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status")
    long countByStatus(@Param("status") Integer status);

    List<Order> findByLabelConvertedFalseAndTaobaoTrackingNoIsNotNull();
}
