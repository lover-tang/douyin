package com.douyin.shipping.mapper;

import com.douyin.shipping.entity.ExpressDelivery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 快递运单Mapper
 */
@Mapper
public interface ExpressDeliveryMapper {

    int insert(ExpressDelivery delivery);

    Optional<ExpressDelivery> findById(Long id);

    Optional<ExpressDelivery> findByLogisticCodeAndShipperCode(@Param("logisticCode") String logisticCode, 
                                                                @Param("shipperCode") String shipperCode);

    List<ExpressDelivery> findByLogisticCode(String logisticCode);

    List<ExpressDelivery> findAll();

    List<ExpressDelivery> findByState(Integer state);

    List<ExpressDelivery> findByIsSubscribed(Integer isSubscribed);

    List<ExpressDelivery> findByLastUpdateTimeBefore(LocalDateTime time);

    int updateState(@Param("id") Long id, @Param("state") Integer state, @Param("location") String location);

    int updateSubscription(@Param("id") Long id, @Param("isSubscribed") Integer isSubscribed, 
                           @Param("callbackUrl") String callbackUrl);

    int updateDeliveryMan(@Param("id") Long id, @Param("name") String name, @Param("tel") String tel);

    int deleteById(Long id);
}
