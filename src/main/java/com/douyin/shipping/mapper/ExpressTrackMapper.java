package com.douyin.shipping.mapper;

import com.douyin.shipping.entity.ExpressTrack;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 快递轨迹Mapper
 */
@Mapper
public interface ExpressTrackMapper {

    int insert(ExpressTrack track);

    Optional<ExpressTrack> findByLogisticCodeAndAcceptTime(@Param("logisticCode") String logisticCode, 
                                                            @Param("acceptTime") LocalDateTime acceptTime);

    List<ExpressTrack> findByExpressIdOrderByAcceptTimeAsc(Long expressId);

    List<ExpressTrack> findByExpressIdOrderByAcceptTimeDesc(Long expressId);

    List<ExpressTrack> findByLogisticCodeOrderByAcceptTimeAsc(String logisticCode);

    List<ExpressTrack> findLatestByLogisticCode(String logisticCode);

    List<ExpressTrack> findByLocationContaining(String location);

    long countByExpressId(Long expressId);

    int deleteByExpressId(Long expressId);

    int deleteById(Long id);
}
