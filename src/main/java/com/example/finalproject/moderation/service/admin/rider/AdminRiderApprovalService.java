package com.example.finalproject.moderation.service.admin.rider;

import com.example.finalproject.communication.domain.Notification;
import com.example.finalproject.communication.enums.NotificationRefType;
import com.example.finalproject.communication.repository.NotificationRepository;
import com.example.finalproject.delivery.domain.Rider;
import com.example.finalproject.delivery.repository.RiderRepository;
import com.example.finalproject.global.exception.custom.BusinessException;
import com.example.finalproject.global.exception.custom.ErrorCode;
import com.example.finalproject.moderation.domain.Approval;
import com.example.finalproject.moderation.domain.ApprovalDocument;
import com.example.finalproject.moderation.dto.admin.rider.AdminRiderApprovalDetailResponse;
import com.example.finalproject.moderation.dto.admin.rider.AdminRiderApprovalListResponse;
import com.example.finalproject.moderation.enums.ApplicantType;
import com.example.finalproject.moderation.enums.ApprovalStatus;
import com.example.finalproject.moderation.repository.ApprovalDocumentRepository;
import com.example.finalproject.moderation.repository.ApprovalRepository;
import com.example.finalproject.user.domain.Role;
import com.example.finalproject.user.domain.User;
import com.example.finalproject.user.domain.UserRole;
import com.example.finalproject.user.repository.RoleRepository;
import com.example.finalproject.user.repository.UserRepository;
import com.example.finalproject.user.repository.UserRoleRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminRiderApprovalService {

    // 보류 기간(일수).
    private static final int HOLD_DAYS = 7;

    private final ApprovalRepository approvalRepository;
    private final ApprovalDocumentRepository approvalDocumentRepository;
    private final RiderRepository riderRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final NotificationRepository notificationRepository;

    // 라이더 승인 대기/보류 목록 조회 (status 리스트로 필터).
    @Transactional(readOnly = true)
    public List<AdminRiderApprovalListResponse> getRiderApprovals(List<ApprovalStatus> statuses) {
        List<ApprovalStatus> targetStatuses = (statuses == null || statuses.isEmpty())
                ? List.of(ApprovalStatus.PENDING, ApprovalStatus.HELD)
                : statuses;

        List<Approval> approvals = approvalRepository.findByApplicantTypeAndStatusIn(
                ApplicantType.RIDER, targetStatuses);
        List<AdminRiderApprovalListResponse> result = new ArrayList<>();

        for (Approval approval : approvals) {
            Rider rider = riderRepository.findByUserId(approval.getUser().getId())
                    .orElse(null);
            if (rider == null) {
                continue;
            }
            result.add(new AdminRiderApprovalListResponse(
                    approval.getId(),
                    rider.getId(),
                    approval.getUser().getId(),
                    approval.getUser().getName(),
                    approval.getStatus(),
                    approval.getCreatedAt(),
                    approval.getHeldUntil()
            ));
        }
        return result;
    }

    // 라이더 승인 상세 조회 (approvalId).
    @Transactional(readOnly = true)
    public AdminRiderApprovalDetailResponse getRiderApprovalDetail(Long approvalId) {
        Approval approval = approvalRepository.findByIdAndApplicantType(approvalId, ApplicantType.RIDER)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE));

        Rider rider = riderRepository.findByUserId(approval.getUser().getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE));

        List<ApprovalDocument> documents = approvalDocumentRepository.findByApprovalId(approvalId);
        List<AdminRiderApprovalDetailResponse.DocumentInfo> documentInfos = new ArrayList<>();
        for (ApprovalDocument doc : documents) {
            documentInfos.add(new AdminRiderApprovalDetailResponse.DocumentInfo(
                    doc.getDocumentType(),
                    doc.getDocumentUrl()
            ));
        }

        AdminRiderApprovalDetailResponse.RiderInfo riderInfo =
                new AdminRiderApprovalDetailResponse.RiderInfo(
                        rider.getId(),
                        approval.getUser().getId(),
                        approval.getUser().getName(),
                        approval.getUser().getPhone(),
                        rider.getIdCardVerified(),
                        rider.getBankName(),
                        rider.getBankAccount(),
                        rider.getAccountHolder()
                );

        return new AdminRiderApprovalDetailResponse(
                approval.getId(),
                approval.getStatus(),
                approval.getReason(),
                approval.getCreatedAt(),
                approval.getApprovedAt(),
                approval.getHeldUntil(),
                riderInfo,
                documentInfos
        );
    }

    // 승인 처리 (approvalId, adminUserId).
    public void approveRider(Long approvalId, Long adminUserId) {
        Approval approval = getRiderApprovalForDecision(approvalId);
        validateStatusForApprove(approval);

        User admin = getAdminUser(adminUserId);
        Rider rider = getRiderByApproval(approval);

        approval.approve(admin);
        rider.approve();
        grantRole(approval.getUser(), "RIDER");

        notificationRepository.save(new Notification(
                approval.getUser(),
                "라이더 승인 완료",
                "라이더 승인 요청이 승인되었습니다.",
                NotificationRefType.RIDER
        ));
    }

    // 보류 처리 (approvalId, adminUserId, reason).
    public void holdRider(Long approvalId, Long adminUserId, String reason) {
        if (!StringUtils.hasText(reason)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        Approval approval = getRiderApprovalForDecision(approvalId);
        if (approval.getStatus() != ApprovalStatus.PENDING) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        User admin = getAdminUser(adminUserId);
        LocalDateTime heldUntil = LocalDateTime.now().plusDays(HOLD_DAYS);
        approval.hold(admin, reason, heldUntil);

        notificationRepository.save(new Notification(
                approval.getUser(),
                "라이더 서류 보류",
                "제출 서류 보완이 필요합니다. 사유: " + reason,
                NotificationRefType.RIDER
        ));
    }

    // 거절 처리 (approvalId, adminUserId, reason).
    public void rejectRider(Long approvalId, Long adminUserId, String reason) {
        if (!StringUtils.hasText(reason)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        Approval approval = getRiderApprovalForDecision(approvalId);
        if (approval.getStatus() != ApprovalStatus.PENDING
                && approval.getStatus() != ApprovalStatus.HELD) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        User admin = getAdminUser(adminUserId);
        Rider rider = getRiderByApproval(approval);

        approval.reject(admin, reason);
        rider.reject();

        notificationRepository.save(new Notification(
                approval.getUser(),
                "라이더 승인 거절",
                "라이더 승인 요청이 거절되었습니다. 사유: " + reason,
                NotificationRefType.RIDER
        ));
    }

    // 라이더 승인 요청 조회 (approvalId).
    private Approval getRiderApprovalForDecision(Long approvalId) {
        return approvalRepository.findByIdAndApplicantType(approvalId, ApplicantType.RIDER)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE));
    }

    // 승인 요청에 해당하는 라이더 조회.
    private Rider getRiderByApproval(Approval approval) {
        return riderRepository.findByUserId(approval.getUser().getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE));
    }

    // 관리자 사용자 조회 (adminUserId).
    private User getAdminUser(Long adminUserId) {
        if (adminUserId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        return userRepository.findById(adminUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE));
    }

    // 승인 시 사용자에게 역할 부여 (중복 방지).
    private void grantRole(User user, String roleName) {
        Role role = roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE));
        if (!userRoleRepository.existsByUserIdAndRoleId(user.getId(), role.getId())) {
            userRoleRepository.save(new UserRole(user, role));
        }
    }

    // 승인 가능한 상태인지 검증.
    private void validateStatusForApprove(Approval approval) {
        ApprovalStatus status = approval.getStatus();
        if (!Objects.equals(status, ApprovalStatus.PENDING)
                && !Objects.equals(status, ApprovalStatus.HELD)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }
}
