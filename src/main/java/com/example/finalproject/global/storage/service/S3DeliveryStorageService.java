package com.example.finalproject.global.storage.service;

import com.example.finalproject.global.storage.service.interfaces.DeliveryStorageService;
import io.awspring.cloud.s3.S3Template;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class S3DeliveryStorageService extends S3StorageService implements DeliveryStorageService {

    public S3DeliveryStorageService(S3Template s3Template, @Value("${custom.s3.bucket}") String bucket) {
        super(s3Template, bucket);
    }

    @Override
    public String uploadDeliveryProofImg(MultipartFile file, String orderId, Long deliveryId) {
        String directoryPath = String.format("order/%s/%d", orderId, deliveryId);

        log.info("StorageService.uploadDeliveryProofImg  deliveryId: {}, path: {}", deliveryId, directoryPath);

        return super.upload(file, directoryPath);
    }
}
