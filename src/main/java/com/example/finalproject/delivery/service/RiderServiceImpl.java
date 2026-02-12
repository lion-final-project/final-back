package com.example.finalproject.delivery.service;

import com.example.finalproject.delivery.domain.Rider;
import com.example.finalproject.delivery.dto.request.PatchRiderStatusRequest;
import com.example.finalproject.delivery.dto.request.PostRiderRegisterRequest;
import com.example.finalproject.delivery.dto.response.GetRiderRegistrationStatusResponse;
import com.example.finalproject.delivery.dto.response.RiderApprovalResponse;
import com.example.finalproject.delivery.dto.response.RiderResponse;
import com.example.finalproject.delivery.enums.RiderApprovalStatus;
import com.example.finalproject.delivery.repository.RiderRepository;
import com.example.finalproject.delivery.service.interfaces.RiderService;
import com.example.finalproject.global.component.UserLoader;
import com.example.finalproject.global.exception.custom.BusinessException;
import com.example.finalproject.global.exception.custom.ErrorCode;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 라이더 정보·승인 관련 서비스 구현체.
 * <p>
 * 라이더 정보 조회, 운행 상태 변경, 등록 신청/삭제를 처리합니다.
 * 위치(Location) 관련 로직은 {@link RiderLocationServiceImpl}로 분리되어 있습니다 (SRP).
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RiderServiceImpl implements RiderService {
    private final RiderRepository riderRepository;
    private final UserLoader userLoader;
    private final ApprovalRepository approvalRepository;

    /** 현재 로그인한 라이더의 정보를 조회합니다. */
    @Override
    public RiderResponse getRiderInfo(String username) {
        Rider rider = findRiderByUsername(username);
        return RiderResponse.from(rider);
    }

    /**
     * 라이더 운행 상태를 변경합니다.
     * <p>
     * ONLINE/OFFLINE만 직접 변경 가능하며, DELIVERING 상태는 배달 수락 시 자동 전환됩니다.
     * 배달 중(DELIVERING)에는 수동 상태 변경이 불가합니다.
     * </p>
     */
    @Override
    @Transactional
    public RiderResponse updateOperationStatus(String username, PatchRiderStatusRequest request) {
        Rider rider = findRiderByUsername(username);

        switch (request.getOperationStatus()) {
            case ONLINE -> rider.goOnline();
            case OFFLINE -> rider.goOffline();
            // DELIVERING은 시스템이 자동 전환. 수동 전환 시도 시 예외
            case DELIVERING -> throw new BusinessException(ErrorCode.RIDER_STATUS_LOCKED_DELIVERING);
        }

        return RiderResponse.from(rider);
    }

    /**
     * 라이더 등록 신청을 생성합니다.
     * <p>
     * 이미 라이더로 등록된 사용자는 기존 Rider 엔티티를 재사용하고,
     * 신규 사용자는 Rider를 새로 생성합니다.
     * 대기(PENDING) 또는 보류(HELD) 상태의 기존 신청이 있으면 중복 신청이 불가합니다.
     * </p>
     */
    @Override
    @Transactional
    public RiderApprovalResponse createApproval(String username, PostRiderRegisterRequest request) {
        User user = userLoader.loadUserByUsername(username);
        Rider rider;

        // 대기/보류 중인 기존 신청 여부 검증
        validateAlreadyPending(user);

        if (riderRepository.existsByUserId(user.getId())) {
            rider = findRiderByUsername(username);
            // 이미 승인된 라이더는 재신청 불가
            if (rider.getStatus() == RiderApprovalStatus.APPROVED) {
                throw new BusinessException(ErrorCode.RIDER_ALREADY_REGISTERED);
            }
            // 재신청 시 신청자 정보 업데이트
            rider.updateApplicantInfo(request.getName(), request.getPhone());
            rider.updateSettlementInfo(request.getBankName(), request.getBankAccount(), request.getAccountHolder());
        } else {
            rider = Rider.builder().user(user)
                    .applicantName(request.getName())
                    .applicantPhone(request.getPhone())
                    .accountHolder(request.getAccountHolder())
                    .bankAccount(request.getBankAccount())
                    .bankName(request.getBankName())
                    .build();
        }

        // 승인 요청 + 서류 첨부
        Approval approval = Approval.builder().user(user)
                .applicantType(ApplicantType.RIDER)
                .build();

        approval.addDocument(DocumentType.ID_CARD, request.getIdCardImage());
        approval.addDocument(DocumentType.BANK_PASSBOOK, request.getBankbookImage());

        riderRepository.save(rider);
        approvalRepository.save(approval);

        return approval.createResponse(rider);
    }

    /** 내 라이더 등록 신청 이력을 페이징 조회합니다. */
    @Override
    public Page<RiderApprovalResponse> getApprovals(String username, Pageable pageable) {
        User user = userLoader.loadUserByUsername(username);
        Optional<Rider> riderOptional = riderRepository.findByUserId(user.getId());
        if (riderOptional.isEmpty()) {
            return Page.empty(pageable);
        }
        Rider rider = riderOptional.get();

        Page<Approval> approvalPage = approvalRepository
                .findApprovalsByUserAndApplicantType(user, ApplicantType.RIDER, pageable);

        return approvalPage.map(approval -> approval.createResponse(rider));
    }

    /**
     * 라이더 등록 신청 상태 조회
     */
    @Override
    public Optional<GetRiderRegistrationStatusResponse> getRegistrationStatus(String username) {
        User user = userLoader.loadUserByUsername(username);

        Optional<Approval> latestApproval = approvalRepository.findTopByUserAndApplicantTypeOrderByIdDesc(
                user, ApplicantType.RIDER);
        if (latestApproval.isPresent()) {
            Approval approval = latestApproval.get();
            return Optional.of(GetRiderRegistrationStatusResponse.builder()
                    .status(approval.getStatus().name())
                    .approvalId(approval.getId())
                    .build());
        }

        return riderRepository.findByUserId(user.getId())
                .map(rider -> GetRiderRegistrationStatusResponse.builder()
                        .status(rider.getStatus().name())
                        .approvalId(null)
                        .build());
    }

    /**
     * 라이더 등록 신청을 삭제합니다.
     * <p>
     * 본인 소유의 PENDING 또는 HELD 상태 신청만 삭제할 수 있습니다.
     * </p>
     *
     * @throws BusinessException APPROVAL_NOT_FOUND / APPROVAL_NOT_OWNED /
     *                           APPROVAL_NOT_PENDING
     */
    @Override
    @Transactional
    public void deleteApproval(String username, Long approvalId) {
        User user = userLoader.loadUserByUsername(username);

        Approval approval = approvalRepository.findById(approvalId)
                .orElseThrow(() -> new BusinessException(ErrorCode.APPROVAL_NOT_FOUND));

        // 본인 소유 확인 — 다른 사용자의 신청 삭제 방지
        if (!approval.getUser().getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.APPROVAL_NOT_OWNED);
        }

        // PENDING 또는 HELD 상태만 삭제 가능 — 이미 처리된 신청은 삭제 불가
        if (!(approval.getStatus() == ApprovalStatus.PENDING || approval.getStatus() == ApprovalStatus.HELD)) {
            throw new BusinessException(ErrorCode.APPROVAL_NOT_PENDING);
        }

        approvalRepository.delete(approval);
    }

    /** 이메일(username)로 라이더를 조회합니다. 없으면 RIDER_NOT_FOUND 예외 */
    private Rider findRiderByUsername(String username) {
        return riderRepository.findByUserEmail(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.RIDER_NOT_FOUND));
    }

    /** PENDING 또는 HELD 상태의 기존 신청이 있는지 확인합니다. 있으면 중복 신청 예외 */
    private void validateAlreadyPending(User user) {
        if (approvalRepository.existsByUserAndApplicantTypeAndStatus(user, ApplicantType.RIDER, ApprovalStatus.PENDING) ||
                approvalRepository.existsByUserAndApplicantTypeAndStatus(user, ApplicantType.RIDER, ApprovalStatus.HELD)) {
            throw new BusinessException(ErrorCode.RIDER_APPROVAL_ALREADY_EXISTS);
        }
    }
}
