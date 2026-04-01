package com.douyin.shipping.service;

import com.douyin.shipping.client.KdniaoClient;
import com.douyin.shipping.client.dto.TraceItem;
import com.douyin.shipping.client.dto.TrackingRequest;
import com.douyin.shipping.client.dto.TrackingResponse;
import com.douyin.shipping.client.enums.KdniaoShipperCode;
import com.douyin.shipping.config.ApiConfig;
import com.douyin.shipping.entity.ExpressTrack;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 快递鸟API测试类 - 测试即时查询接口(8002/1002)并保存到数据库
 */
@Slf4j
@SpringBootTest
public class KdniaoApiServiceTest {

    @Autowired
    private KdniaoClient kdniaoClient;

    @Autowired
    private KdniaoService kdniaoService;

    @Autowired
    private ExpressTrackService expressTrackService;

    @Autowired
    private ApiConfig apiConfig;

    /**
     * 测试快递鸟即时查询接口(8002)并将结果保存到数据库
     * 使用快递鸟提供的测试单号
     */
    @Test
    public void testQueryTrackingAndSaveToDb() {
        // 快递鸟测试环境测试单号（参考官方示例）
//        String shipperCode = KdniaoShipperCode.ZTO.getCode();
        String logisticCode = "9815069645647";

        log.info("开始测试快递鸟即时查询接口(8002): logisticCode={}", logisticCode);

        // 调试：打印配置信息
        log.info("===== 调试配置信息 =====");
        log.info("EBusinessID: {}", apiConfig.getKdniao().getEBusinessId());
        log.info("API Key: {}", apiConfig.getKdniao().getApiKey() != null ? apiConfig.getKdniao().getApiKey().substring(0, Math.min(10, apiConfig.getKdniao().getApiKey().length())) + "..." : "null");
        log.info("Request URL: {}", apiConfig.getKdniao().getReqUrl());
        log.info("========================");

        // 构建查询请求
        TrackingRequest request = new TrackingRequest();
        request.setLogisticCode(logisticCode);

        // 调用快递鸟接口查询
        TrackingResponse response = kdniaoService.queryTracking(request);

        log.info("查询结果: success={}, message={}", response.isSuccess(), response.getMessage());

        if (response.isSuccess()) {
            log.info("物流状态: {} ({})", response.getState(), kdniaoService.getStateDescription(response.getState()));
            log.info("快递公司: {}", response.getShipperCode());
            log.info("快递单号: {}", response.getLogisticCode());

            List<TraceItem> traces = response.getTraces();
            if (traces != null && !traces.isEmpty()) {
                log.info("获取到 {} 条轨迹记录，准备保存到数据库", traces.size());

                // 将轨迹数据转换为实体并保存到数据库
                List<ExpressTrack> trackEntities = convertToEntities(response);
                expressTrackService.saveTracks(trackEntities);

                log.info("成功保存 {} 条轨迹记录到数据库", trackEntities.size());

                // 打印轨迹信息
                for (ExpressTrack track : trackEntities) {
                    log.info("  [{}] {} - {}",
                            track.getAcceptTime(),
                            track.getAcceptStation(),
                            track.getLocation() != null ? track.getLocation() : "");
                }
            } else {
                log.info("暂无轨迹记录");
            }

            assertTrue(response.isSuccess(), "查询应成功");
        } else {
            log.error("查询失败: {}", response.getMessage());
            // 如果是配置问题，测试不应该失败，只是记录警告
            if (response.getMessage() != null && response.getMessage().contains("EBusinessID")) {
                log.warn("请检查 application.properties 中的快递鸟API配置 (kdniao.e-business-id 和 kdniao.api-key)");
            }
        }
    }

    /**
     * 将TrackingResponse转换为ExpressTrack实体列表
     */
    private List<ExpressTrack> convertToEntities(TrackingResponse response) {
        List<ExpressTrack> tracks = new ArrayList<>();

        if (response.getTraces() == null || response.getTraces().isEmpty()) {
            return tracks;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        int order = 0;

        for (TraceItem trace : response.getTraces()) {
            ExpressTrack track = new ExpressTrack();
            track.setLogisticCode(response.getLogisticCode());
            track.setShipperCode(response.getShipperCode());
            track.setAcceptStation(trace.getAcceptStation());
            track.setLocation(trace.getLocation());
            track.setTrackOrder(order++);
            track.setCreateTime(LocalDateTime.now());

            // 解析时间
            if (trace.getAcceptTime() != null && !trace.getAcceptTime().isEmpty()) {
                try {
                    track.setAcceptTime(LocalDateTime.parse(trace.getAcceptTime(), formatter));
                } catch (Exception e) {
                    log.warn("解析时间失败: {}", trace.getAcceptTime());
                    track.setAcceptTime(LocalDateTime.now());
                }
            } else {
                track.setAcceptTime(LocalDateTime.now());
            }

            tracks.add(track);
        }

        return tracks;
    }
}
