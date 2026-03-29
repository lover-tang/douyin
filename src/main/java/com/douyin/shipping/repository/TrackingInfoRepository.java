package com.douyin.shipping.repository;

import com.douyin.shipping.entity.TrackingInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrackingInfoRepository extends JpaRepository<TrackingInfo, Long> {

    List<TrackingInfo> findByOrderIdOrderByTrackingTimeDesc(Long orderId);

    List<TrackingInfo> findByTrackingNoOrderByTrackingTimeDesc(String trackingNo);

    List<TrackingInfo> findBySyncedFalse();

    List<TrackingInfo> findByOrderIdAndSyncedFalse(Long orderId);

    long countByOrderId(Long orderId);

    long countBySyncedFalse();
}
