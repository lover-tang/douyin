package com.douyin.shipping.service;

import com.douyin.shipping.entity.ExpressTrack;
import com.douyin.shipping.mapper.ExpressTrackMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 快递轨迹Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExpressTrackService {

    private final ExpressTrackMapper expressTrackMapper;

    /**
     * 保存轨迹（如果已存在则跳过）
     */
    @Transactional
    public ExpressTrack saveTrack(ExpressTrack track) {
        Optional<ExpressTrack> existing = expressTrackMapper
            .findByLogisticCodeAndAcceptTime(track.getLogisticCode(), track.getAcceptTime());
        
        if (existing.isPresent()) {
            log.debug("轨迹已存在，跳过: logisticCode={}, acceptTime={}", 
                track.getLogisticCode(), track.getAcceptTime());
            return existing.get();
        }
        
        expressTrackMapper.insert(track);
        log.info("保存轨迹成功: id={}, logisticCode={}", track.getId(), track.getLogisticCode());
        return track;
    }

    /**
     * 批量保存轨迹
     */
    @Transactional
    public void saveTracks(List<ExpressTrack> tracks) {
        for (ExpressTrack track : tracks) {
            saveTrack(track);
        }
    }

    /**
     * 根据快递主表ID查询轨迹（正序）
     */
    public List<ExpressTrack> findByExpressIdOrderByTimeAsc(Long expressId) {
        return expressTrackMapper.findByExpressIdOrderByAcceptTimeAsc(expressId);
    }

    /**
     * 根据快递主表ID查询轨迹（倒序）
     */
    public List<ExpressTrack> findByExpressIdOrderByTimeDesc(Long expressId) {
        return expressTrackMapper.findByExpressIdOrderByAcceptTimeDesc(expressId);
    }

    /**
     * 根据物流单号查询轨迹
     */
    public List<ExpressTrack> findByLogisticCode(String logisticCode) {
        return expressTrackMapper.findByLogisticCodeOrderByAcceptTimeAsc(logisticCode);
    }

    /**
     * 查询最新轨迹
     */
    public List<ExpressTrack> findLatestByLogisticCode(String logisticCode) {
        return expressTrackMapper.findLatestByLogisticCode(logisticCode);
    }

    /**
     * 根据城市查询轨迹
     */
    public List<ExpressTrack> findByLocation(String location) {
        return expressTrackMapper.findByLocationContaining(location);
    }

    /**
     * 统计轨迹数量
     */
    public long countByExpressId(Long expressId) {
        return expressTrackMapper.countByExpressId(expressId);
    }

    /**
     * 删除指定快递的所有轨迹
     */
    @Transactional
    public void deleteByExpressId(Long expressId) {
        expressTrackMapper.deleteByExpressId(expressId);
        log.info("删除轨迹成功: expressId={}", expressId);
    }

    /**
     * 根据ID删除轨迹
     */
    @Transactional
    public void deleteById(Long id) {
        expressTrackMapper.deleteById(id);
        log.info("删除轨迹成功: id={}", id);
    }
}
