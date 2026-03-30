package com.douyin.shipping.mapper;

import com.douyin.shipping.entity.ExpressSubscription;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 物流订阅Mapper
 */
@Mapper
public interface ExpressSubscriptionMapper {

    int insert(ExpressSubscription subscription);

    Optional<ExpressSubscription> findById(Long id);

    Optional<ExpressSubscription> findByExpressId(Long expressId);

    List<ExpressSubscription> findByLogisticCode(String logisticCode);

    List<ExpressSubscription> findBySubscriptionStatus(Integer subscriptionStatus);

    List<ExpressSubscription> findAll();

    List<ExpressSubscription> findByExpireTimeBeforeAndSubscriptionStatus(@Param("time") LocalDateTime time, 
                                                                          @Param("status") Integer status);

    int updatePushStats(@Param("id") Long id, @Param("pushTime") LocalDateTime pushTime, 
                        @Param("successCount") int successCount, @Param("failCount") int failCount);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    int deleteById(Long id);

    int deleteByExpressId(Long expressId);
}
