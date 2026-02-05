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

    // 蹂대쪟 湲곌컙(?쇱닔).
    private static final int HOLD_DAYS = 7;

    private final ApprovalRepository approvalRepository;
    private final ApprovalDocumentRepository approvalDocumentRepository;
    private final RiderRepository riderRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final NotificationRepository notificationRepository;

    // ?쇱씠???뱀씤 ?湲?蹂대쪟 紐⑸줉 議고쉶 (status 由ъ뒪??.
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

    // ?쇱씠???뱀씤 ?곸꽭 議고쉶 (approvalId).
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

    // ?뱀씤 泥섎━ (approvalId, adminUserId).
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
                "?쇱씠???뱀씤 ?꾨즺",
                "?쇱씠???뱀씤 ?붿껌???뱀씤?섏뿀?듬땲??",
                NotificationRefType.RIDER
        ));
    }

    // 蹂대쪟 泥섎━ (approvalId, adminUserId, reason).
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
                "?쇱씠???쒕쪟 蹂대쪟",
                "?쒖텧 ?쒕쪟 蹂댁셿???꾩슂?⑸땲?? ?ъ쑀: " + reason,
                NotificationRefType.RIDER
        ));
    }

    // 嫄곗젅 泥섎━ (approvalId, adminUserId, reason).
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
                "?쇱씠???뱀씤 嫄곗젅",
                "?쇱씠???뱀씤 ?붿껌??嫄곗젅?섏뿀?듬땲?? ?ъ쑀: " + reason,
                NotificationRefType.RIDER
        ));
    }

    // ?쇱씠???뱀씤 ???議고쉶 (approvalId).
    private Approval getRiderApprovalForDecision(Long approvalId) {
        return approvalRepository.findByIdAndApplicantType(approvalId, ApplicantType.RIDER)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE));
    }

    // ?뱀씤 ?붿껌???곌껐???쇱씠??議고쉶.
    private Rider getRiderByApproval(Approval approval) {
        return riderRepository.findByUserId(approval.getUser().getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE));
    }

    // 愿由ъ옄 ?ъ슜??議고쉶 (adminUserId).
    private User getAdminUser(Long adminUserId) {
        if (adminUserId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        return userRepository.findById(adminUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE));
    }

    // 승인 대상 사용자에게 역할 부여 (중복 시 스킵).
    private void grantRole(User user, String roleName) {
        Role role = roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE));
        if (!userRoleRepository.existsByUserIdAndRoleId(user.getId(), role.getId())) {
            userRoleRepository.save(new UserRole(user, role));
        }
    }

    // ?뱀씤 媛?ν븳 ?곹깭?몄? 寃利?
    private void validateStatusForApprove(Approval approval) {
        ApprovalStatus status = approval.getStatus();
        if (!Objects.equals(status, ApprovalStatus.PENDING)
                && !Objects.equals(status, ApprovalStatus.HELD)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }
}

