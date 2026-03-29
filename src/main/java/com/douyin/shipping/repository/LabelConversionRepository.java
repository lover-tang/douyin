package com.douyin.shipping.repository;

import com.douyin.shipping.entity.LabelConversion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LabelConversionRepository extends JpaRepository<LabelConversion, Long> {

    Optional<LabelConversion> findByOrderId(Long orderId);

    List<LabelConversion> findByStatus(Integer status);

    Optional<LabelConversion> findByDouyinOrderNo(String douyinOrderNo);
}
