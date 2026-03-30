package com.douyin.shipping.service;

import com.douyin.shipping.entity.ExpressDelivery;
import com.douyin.shipping.mapper.ExpressDeliveryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 快递运单Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExpressDeliveryService {

    private final ExpressDeliveryMapper expressDeliveryMapper;

    /**
     * 创建运单
     */
    @Transactional
    public ExpressDelivery createDelivery(ExpressDelivery delivery) {
        Optional<ExpressDelivery> existing = expressDeliveryMapper
            .findByLogisticCodeAndShipperCode(delivery.getLogisticCode(), delivery.getShipperCode());
        
        if (existing.isPresent()) {
            log.warn("运单已存在: logisticCode={}, shipperCode={}", 
                delivery.getLogisticCode(), delivery.getShipperCode());
            return existing.get();
        }
        
        expressDeliveryMapper.insert(delivery);
        log.info("创建运单成功: id={}, logisticCode={}", delivery.getId(), delivery.getLogisticCode());
        return delivery;
    }

    /**
     * 根据ID查询
     */
    public Optional<ExpressDelivery> findById(Long id) {
        return expressDeliveryMapper.findById(id);
    }

    /**
     * 根据物流单号和快递公司编码查询
     */
    public Optional<ExpressDelivery> findByLogisticCodeAndShipperCode(String logisticCode, String shipperCode) {
        return expressDeliveryMapper.findByLogisticCodeAndShipperCode(logisticCode, shipperCode);
    }

    /**
     * 根据物流单号查询
     */
    public List<ExpressDelivery> findByLogisticCode(String logisticCode) {
        return expressDeliveryMapper.findByLogisticCode(logisticCode);
    }

    /**
     * 查询所有运单
     */
    public List<ExpressDelivery> findAll() {
        return expressDeliveryMapper.findAll();
    }

    /**
     * 根据状态查询
     */
    public List<ExpressDelivery> findByState(Integer state) {
        return expressDeliveryMapper.findByState(state);
    }

    /**
     * 更新物流状态
     */
    @Transactional
    public boolean updateState(Long id, Integer state, String location) {
        int updated = expressDeliveryMapper.updateState(id, state, location);
        if (updated > 0) {
            log.info("更新运单状态成功: id={}, state={}, location={}", id, state, location);
        }
        return updated > 0;
    }

    /**
     * 更新订阅状态
     */
    @Transactional
    public boolean updateSubscription(Long id, Integer isSubscribed, String callbackUrl) {
        int updated = expressDeliveryMapper.updateSubscription(id, isSubscribed, callbackUrl);
        if (updated > 0) {
            log.info("更新订阅状态成功: id={}, isSubscribed={}", id, isSubscribed);
        }
        return updated > 0;
    }

    /**
     * 更新派送员信息
     */
    @Transactional
    public boolean updateDeliveryMan(Long id, String name, String tel) {
        int updated = expressDeliveryMapper.updateDeliveryMan(id, name, tel);
        if (updated > 0) {
            log.info("更新派送员信息成功: id={}, name={}", id, name);
        }
        return updated > 0;
    }

    /**
     * 删除运单
     */
    @Transactional
    public void deleteById(Long id) {
        expressDeliveryMapper.deleteById(id);
        log.info("删除运单成功: id={}", id);
    }

    /**
     * 查询长时间未更新的运单
     */
    public List<ExpressDelivery> findStaleDeliveries(int hours) {
        LocalDateTime time = LocalDateTime.now().minusHours(hours);
        return expressDeliveryMapper.findByLastUpdateTimeBefore(time);
    }
}
