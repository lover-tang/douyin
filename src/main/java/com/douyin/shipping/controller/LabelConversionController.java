package com.douyin.shipping.controller;

import com.douyin.shipping.dto.ApiResult;
import com.douyin.shipping.dto.LabelConvertRequest;
import com.douyin.shipping.entity.LabelConversion;
import com.douyin.shipping.entity.LogisticsMapping;
import com.douyin.shipping.service.LabelConversionService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/labels")
@RequiredArgsConstructor
public class LabelConversionController {

    private final LabelConversionService labelConversionService;

    /**
     * 面单转换
     */
    @PostMapping("/convert")
    public ApiResult<LabelConversion> convertLabel(@Validated @RequestBody LabelConvertRequest request) {
        try {
            return ApiResult.success(labelConversionService.convertLabel(request));
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 批量面单转换
     */
    @PostMapping("/convert/batch")
    public ApiResult<Integer> batchConvert(@RequestBody List<LabelConvertRequest> requests) {
        try {
            return ApiResult.success(labelConversionService.batchConvert(requests));
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 查询面单转换记录
     */
    @GetMapping("/order/{orderId}")
    public ApiResult<LabelConversion> getConversionByOrderId(@PathVariable Long orderId) {
        LabelConversion conversion = labelConversionService.getConversionByOrderId(orderId);
        return conversion != null ? ApiResult.success(conversion) : ApiResult.error("未找到转换记录");
    }

    /**
     * 获取所有面单转换记录
     */
    @GetMapping
    public ApiResult<List<LabelConversion>> getAllConversions() {
        return ApiResult.success(labelConversionService.getAllConversions());
    }

    /**
     * 获取物流公司映射列表
     */
    @GetMapping("/logistics-mappings")
    public ApiResult<List<LogisticsMapping>> getLogisticsMappings() {
        return ApiResult.success(labelConversionService.getLogisticsMappings());
    }

    /**
     * 添加物流公司映射
     */
    @PostMapping("/logistics-mappings")
    public ApiResult<LogisticsMapping> addLogisticsMapping(@RequestBody LogisticsMapping mapping) {
        try {
            return ApiResult.success(labelConversionService.addLogisticsMapping(mapping));
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 删除物流公司映射
     */
    @DeleteMapping("/logistics-mappings/{id}")
    public ApiResult<Void> deleteLogisticsMapping(@PathVariable Long id) {
        try {
            labelConversionService.deleteLogisticsMapping(id);
            return ApiResult.success();
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }
}
