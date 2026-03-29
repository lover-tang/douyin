package com.douyin.shipping.repository;

import com.douyin.shipping.entity.LogisticsMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface LogisticsMappingRepository extends JpaRepository<LogisticsMapping, Long> {

    Optional<LogisticsMapping> findByTaobaoName(String taobaoName);

    Optional<LogisticsMapping> findByTaobaoCode(String taobaoCode);

    List<LogisticsMapping> findByEnabledTrue();
}
