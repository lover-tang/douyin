package com.douyin.shipping.service;

import com.douyin.shipping.entity.ExpressSubscription;
import com.douyin.shipping.mapper.ExpressSubscriptionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 物流订阅Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExpressSubscriptionService {

    private final ExpressSubscriptionMapper expressSubscriptionMapper;

    /**
     * 创建订阅
     */
    @Transactional
    public ExpressSubscription createSubscription(ExpressSubscription subscription) {
        Optional<ExpressSubscription> existing = expressSubscriptionMapper
            .findByExpressId(subscription.getExpressId());
        
        if (existing.isPresent()) {
            log.warn("订阅已存在: expressId={}", subscription.getExpressId());
            return existing.get();
        }
        
        expressSubscriptionMapper.insert(subscription);
        log.info("创建订阅成功: id={}, expressId={}", subscription.getId(), subscription.getExpressId());
        return subscription;
    }

    /**
     * 根据ID查询
     */
    public Optional<ExpressSubscription> findById(Long id) {
        return expressSubscriptionMapper.findById(id);
    }

    /**
     * 根据快递主表ID查询
     */
    public Optional<ExpressSubscription> findByExpressId(Long expressId) {
        return expressSubscriptionMapper.findByExpressId(expressId);
    }

    /**
     * 根据物流单号查询
     */
    public List<ExpressSubscription> findByLogisticCode(String logisticCode) {
        return expressSubscriptionMapper.findByLogisticCode(logisticCode);
    }

    /**
     * 根据状态查询
     */
    public List<ExpressSubscription> findByStatus(Integer status) {
        return expressSubscriptionMapper.findBySubscriptionStatus(status);
    }

    /**
     * 查询所有订阅
     */
    public List<ExpressSubscription> findAll() {
        return expressSubscriptionMapper.findAll();
    }

    /**
     * 更新推送统计
     */
    @Transactional
    public boolean updatePushStats(Long id, int successCount, int failCount) {
        int updated = expressSubscriptionMapper.updatePushStats(id, LocalDateTime.now(), successCount, failCount);
        if (updated > 0) {
            log.info("更新推送统计成功: id={}, success={}, fail={}", id, successCount, failCount);
        }
        return updated > 0;
    }

    /**
     * 更新订阅状态
     */
    @Transactional
    public boolean updateStatus(Long id, Integer status) {
        int updated = expressSubscriptionMapper.updateStatus(id, status);
        if (updated > 0) {
            log.info("更新订阅状态成功: id={}, status={}", id, status);
        }
        return updated > 0;
    }

    /**
     * 取消订阅
     */
    @Transactional
    public boolean cancelSubscription(Long id) {
        return updateStatus(id, 0);
    }

    /**
     * 查询即将过期的订阅
     */
    public List<ExpressSubscription> findExpiringSubscriptions(int hours) {
        LocalDateTime time = LocalDateTime.now().plusHours(hours);
        return expressSubscriptionMapper.findByExpireTimeBeforeAndSubscriptionStatus(time, 1);
    }

    /**
     * 删除订阅
     */
    @Transactional
    public void deleteById(Long id) {
        expressSubscriptionMapper.deleteById(id);
        log.info("删除订阅成功: id={}", id);
    }

    /**
     * 根据快递主表ID删除订阅
     */
    @Transactional
    public void deleteByExpressId(Long expressId) {
        expressSubscriptionMapper.deleteByExpressId(expressId);
        log.info("删除订阅成功: expressId={}", expressId);
    }
}
