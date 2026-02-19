package com.example.finalproject.global.storage.service.interfaces;

import com.example.finalproject.moderation.enums.ApplicantType;
import com.example.finalproject.moderation.enums.DocumentType;
import org.springframework.web.multipart.MultipartFile;

public interface DeliveryStorageService extends StorageService{
////   order/
//     └── {orderId}/                    # 주문 식별 (보안 및 관리 중심)
//            ├── {deliveryId}/          # 배달 식별 (ApplicantType 사용)
//            │     └── img.jpg          # 파일명은 UUID_DocumentType.확장자 사용
//            └── {deliveryId}/
//                  └── img.png

    /****/
    String uploadDeliveryProofImg(MultipartFile file, String orderId, Long deliveryId);
}
