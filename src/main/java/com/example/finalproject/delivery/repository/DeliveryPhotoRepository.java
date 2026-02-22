package com.example.finalproject.delivery.repository;

import com.example.finalproject.delivery.domain.DeliveryPhoto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 배달 사진 레포지토리.
 * 배달 완료/문제 신고 시 첨부된 사진을 관리합니다.
 */
public interface DeliveryPhotoRepository extends JpaRepository<DeliveryPhoto, Long> {

    /** 특정 배달에 첨부된 사진 목록 조회 */
    List<DeliveryPhoto> findByDeliveryId(Long deliveryId);

    List<DeliveryPhoto> findByDeliveryIdOrderByCreatedAtDesc(Long deliveryId);
}
