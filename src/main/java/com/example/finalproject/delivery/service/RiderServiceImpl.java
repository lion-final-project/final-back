package com.example.finalproject.delivery.service;

import com.example.finalproject.delivery.domain.Rider;
import com.example.finalproject.delivery.dto.request.PatchRiderStatusRequest;
import com.example.finalproject.delivery.dto.request.PostRiderLocationRequest;
import com.example.finalproject.delivery.dto.request.PostRiderRegisterRequest;
import com.example.finalproject.delivery.dto.response.GetRiderLocationResponse;
import com.example.finalproject.delivery.dto.response.RiderApprovalResponse;
import com.example.finalproject.delivery.dto.response.RiderResponse;
import com.example.finalproject.delivery.enums.RiderOperationStatus;
import com.example.finalproject.delivery.repository.RiderRepository;
import com.example.finalproject.delivery.service.interfaces.RiderLocationService;
import com.example.finalproject.delivery.service.interfaces.RiderService;
import com.example.finalproject.global.component.UserLoader;
import com.example.finalproject.global.util.GeometryUtil;
import com.example.finalproject.moderation.domain.Approval;
import com.example.finalproject.moderation.enums.ApplicantType;
import com.example.finalproject.moderation.enums.ApprovalStatus;
import com.example.finalproject.moderation.enums.DocumentType;
import com.example.finalproject.moderation.repository.ApprovalRepository;
import com.example.finalproject.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RiderServiceImpl implements RiderService, RiderLocationService {
    private final RiderRepository riderRepository;
    private final UserLoader userLoader;
    private final ApprovalRepository approvalRepository;
    private final DeliveryMatchingService deliveryMatchingService;

    private final StringRedisTemplate redisTemplate;
    private static final String RIDER_GEO_KEY = "rider:locations";

    @Override
    public RiderResponse getRiderInfo(String username) {
        Rider rider = findRiderByUsername(username);
        return rider.createResponse();
    }

    @Override
    @Transactional
    public RiderResponse updateOperationStatus(String username, PatchRiderStatusRequest request) {

        Rider rider = findRiderByUsername(username);
        if (rider.getOperationStatus() == RiderOperationStatus.DELIVERING) {
            throw new RuntimeException("Status locked: DELIVERING");
        }

        rider.setOperationStatus(request.getOperationStatus());
        riderRepository.save(rider);

        return rider.createResponse();
    }

    @Override
    @Transactional
    public RiderApprovalResponse createApproval(String username, PostRiderRegisterRequest request) {
        User user = userLoader.loadUserByUsername(username);
        Rider rider;

        if (riderRepository.existsByUserId(user.getId())) {
            rider = findRiderByUsername(username);
        } else {
            validateAlreadyPending(user);

            rider = Rider.builder().user(user)
                    .accountHolder(request.getAccountHolder())
                    .bankAccount(request.getBankAccount())
                    .bankName(request.getBankName())
                    .build();
        }

        Approval approval = Approval.builder().user(user)
                .applicantType(ApplicantType.RIDER)
                .build();

        approval.addDocument(DocumentType.ID_CARD, request.getIdCardImage());
        approval.addDocument(DocumentType.BANK_PASSBOOK, request.getBankbookImage());

        riderRepository.save(rider);
        approvalRepository.save(approval);

        return approval.createResponse(rider);
    }

    @Override
    public Page<RiderApprovalResponse> getApprovals(String username, Pageable pageable) {
        User user = userLoader.loadUserByUsername(username);
        if (!riderRepository.existsByUserId(user.getId())) {
            throw new RuntimeException("Rider not found");
        }
        Rider rider = findRiderByUsername(username);

        Page<Approval> approvalPage = approvalRepository
                .findApprovalsByUserAndApplicantType(user, ApplicantType.RIDER, pageable);

        return approvalPage.map(approval -> approval.createResponse(rider));
    }

    @Override
    @Transactional
    public void deleteApproval(Long approvalId) {
        Approval approval = approvalRepository.findById(approvalId)
                .orElseThrow(() -> new RuntimeException("Approval not found"));

        if (!(approval.getStatus() == ApprovalStatus.PENDING)) {
            throw new RuntimeException("Approval status is not PENDING");
        }

        approvalRepository.delete(approval);
    }

    // 라이더 조회
    private Rider findRiderByUsername(String username) {
        return riderRepository.findByUserEmail(username)
                .orElseThrow(() -> new RuntimeException("Rider not found"));
    }

    // 신청이력 삭제 가능여부 확인
    private void validateAlreadyPending(User user) throws RuntimeException {

        if (approvalRepository.existsByUserAndStatus(user, ApprovalStatus.PENDING) ||
                approvalRepository.existsByUserAndStatus(user, ApprovalStatus.HELD)) {
            throw new RuntimeException("Cannot delete: Not PENDING or HELD");
        }
    }

    @Override
    public void updateRiderLocation(PostRiderLocationRequest request) {
        log.info("updateRiderLocation 호출 - riderId: {}, lon: {}, lat: {}",
                request.getRiderId(), request.getLongitude(), request.getLatitude());
        try {
            Point point = GeometryUtil.createPointForRedis(request.getLongitude(), request.getLatitude());
            log.info("Point 생성 결과: {}", point);
            redisTemplate.opsForGeo().add(RIDER_GEO_KEY, point, request.getRiderId());

            // 2. 주변 배달 목록 갱신 트리거
            deliveryMatchingService.updateRiderNearbyDeliveries(
                    Long.parseLong(request.getRiderId()),
                    request.getLongitude(),
                    request.getLatitude());
        } catch (Exception e) {
            log.error("Redis GEO 저장 실패", e);
            throw e;
        }
    }

    @Override
    public void removeRider(String riderId) {
        redisTemplate.opsForGeo().remove(RIDER_GEO_KEY, riderId);
    }

    @Override
    public GetRiderLocationResponse getRiderLocation(String riderId) {
        List<Point> positions = redisTemplate.opsForGeo().position(RIDER_GEO_KEY, riderId);

        if (positions == null || positions.isEmpty() || positions.getFirst() == null) {
            throw new RuntimeException("라이더 위치 정보를 찾을 수 없습니다.");
        }

        Point point = positions.getFirst();
        return GetRiderLocationResponse.builder()
                .riderId(riderId)
                .longitude(point.getX())
                .latitude(point.getY())
                .build();
    }
}
