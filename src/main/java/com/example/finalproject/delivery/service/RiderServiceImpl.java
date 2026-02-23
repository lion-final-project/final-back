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
import com.example.finalproject.communication.enums.NotificationRefType;
import com.example.finalproject.communication.service.NotificationService;
import com.example.finalproject.moderation.domain.Approval;
import com.example.finalproject.moderation.enums.ApplicantType;
import com.example.finalproject.moderation.enums.ApprovalStatus;
import com.example.finalproject.moderation.enums.DocumentType;
import com.example.finalproject.moderation.repository.ApprovalRepository;
import com.example.finalproject.user.domain.User;
import com.example.finalproject.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * ??깆뵠???類ｋ궖夷?諭???온????뺥돩???닌뗭겱筌?
 * <p>
 * ??깆뵠???類ｋ궖 鈺곌퀬?? ??꾨뻬 ?怨밴묶 癰궰野? ?源낆쨯 ?醫롪퍕/???ｇ몴?筌ｌ꼶???몃빍??
 * ?袁⑺뒄(Location) ?온??嚥≪뮇彛?? {@link RiderLocationServiceImpl}嚥??브쑬???뤿선 ??됰뮸??덈뼄 (SRP).
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
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    /** ?袁⑹삺 嚥≪뮄??紐낅립 ??깆뵠?遺우벥 ?類ｋ궖??鈺곌퀬???몃빍?? */
    @Override
    public RiderResponse getRiderInfo(String username) {
        Rider rider = findRiderByUsername(username);
        return RiderResponse.from(rider);
    }

    /**
     * ??깆뵠????꾨뻬 ?怨밴묶??癰궰野껋?鍮??덈뼄.
     * <p>
     * ONLINE/OFFLINE筌?筌욊낯??癰궰野?揶쎛?館釉?쭖? DELIVERING ?怨밴묶??獄쏄퀡????롮뵭 ???癒?짗 ?袁れ넎??몃빍??
     * 獄쏄퀡??餓?DELIVERING)?癒?뮉 ??롫짗 ?怨밴묶 癰궰野껋럩???븍뜃???몃빍??
     * </p>
     */
    @Override
    @Transactional
    public RiderResponse updateOperationStatus(String username, PatchRiderStatusRequest request) {
        Rider rider = findRiderByUsername(username);

        switch (request.getOperationStatus()) {
            case ONLINE -> rider.goOnline();
            case OFFLINE -> rider.goOffline();
            // DELIVERING?? ??뽯뮞??뽰뵠 ?癒?짗 ?袁れ넎. ??롫짗 ?袁れ넎 ??뺣즲 ????됱뇚
            case DELIVERING -> throw new BusinessException(ErrorCode.RIDER_STATUS_LOCKED_DELIVERING);
        }

        return RiderResponse.from(rider);
    }

    /**
     * ??깆뵠???源낆쨯 ?醫롪퍕????밴쉐??몃빍??
     * <p>
     * ??? ??깆뵠?遺얠쨮 ?源낆쨯??????癒?뮉 疫꿸퀣??Rider ?酉??怨? ??沅??븍릭??
     * ?醫됲뇣 ????癒?뮉 Rider????덉쨮 ??밴쉐??몃빍??
     * ??疫?PENDING) ?癒?뮉 癰귣?履?HELD) ?怨밴묶??疫꿸퀣???醫롪퍕????됱몵筌?餓λ쵎???醫롪퍕???븍뜃???몃빍??
     * </p>
     */
    @Override
    @Transactional
    public RiderApprovalResponse createApproval(String username, PostRiderRegisterRequest request) {
        User user = userLoader.loadUserByUsername(username);
        Rider rider;

        // ??疫?癰귣?履?餓λ쵐??疫꿸퀣???醫롪퍕 ??? 野꺜筌?
        validateReapplyAllowed(user);
        Optional<Approval> latestApproval = approvalRepository.findTopByUserAndApplicantTypeOrderByIdDesc(
                user, ApplicantType.RIDER);

        if (riderRepository.existsByUserId(user.getId())) {
            rider = findRiderByUsername(username);
            // ??? ?諭?????깆뵠?遺얜뮉 ???딉㎗??븍뜃?
            if (rider.getStatus() == RiderApprovalStatus.APPROVED) {
                throw new BusinessException(ErrorCode.RIDER_ALREADY_REGISTERED);
            }
            // ???딉㎗????醫롪퍕???類ｋ궖 ??낅쑓??꾨뱜
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

        // HELD 건은 동일 신청 건을 PENDING으로 복구하여 재심사한다.
        Approval approval;
        if (latestApproval.isPresent() && latestApproval.get().getStatus() == ApprovalStatus.HELD) {
            approval = latestApproval.get();
            approval.resubmit();
            approval.clearDocuments();
            // 기존 문서 삭제 SQL을 먼저 반영해야 (approval_id, document_type) 유니크 충돌을 피할 수 있다.
            approvalRepository.saveAndFlush(approval);
        } else {
            approval = Approval.builder().user(user)
                    .applicantType(ApplicantType.RIDER)
                    .build();
        }

        approval.addDocument(DocumentType.ID_CARD, request.getIdCardImage());
        approval.addDocument(DocumentType.BANK_PASSBOOK, request.getBankbookImage());

        riderRepository.save(rider);
        approvalRepository.save(approval);
        notifyAdminsForRiderSubmission(user, rider);

        return approval.createResponse(rider);
    }

    /** ????깆뵠???源낆쨯 ?醫롪퍕 ???????륁뵠筌?鈺곌퀬???몃빍?? */
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
     * ??깆뵠???源낆쨯 ?醫롪퍕 ?怨밴묶 鈺곌퀬??     */
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
                    .reason(approval.getReason())
                    .heldUntil(approval.getHeldUntil())
                    .build());
        }

        return riderRepository.findByUserId(user.getId())
                .map(rider -> GetRiderRegistrationStatusResponse.builder()
                        .status(rider.getStatus().name())
                        .approvalId(null)
                        .reason(null)
                        .heldUntil(null)
                        .build());
    }

    /**
     * ??깆뵠???源낆쨯 ?醫롪퍕???????몃빍??
     * <p>
     * 癰귣챷????????PENDING ?癒?뮉 HELD ?怨밴묶 ?醫롪퍕筌??????????됰뮸??덈뼄.
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

        // 癰귣챷?????? ?類ㅼ뵥 ????삘뀲 ????癒?벥 ?醫롪퍕 ????獄쎻뫗?
        if (!approval.getUser().getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.APPROVAL_NOT_OWNED);
        }

        // PENDING ?癒?뮉 HELD ?怨밴묶筌?????揶쎛??????? 筌ｌ꼶????醫롪퍕?? ?????븍뜃?
        if (!(approval.getStatus() == ApprovalStatus.PENDING || approval.getStatus() == ApprovalStatus.HELD)) {
            throw new BusinessException(ErrorCode.APPROVAL_NOT_PENDING);
        }

        approvalRepository.delete(approval);
    }

    /** ??李??username)嚥???깆뵠?遺? 鈺곌퀬???몃빍?? ??곸몵筌?RIDER_NOT_FOUND ??됱뇚 */
    private Rider findRiderByUsername(String username) {
        return riderRepository.findByUserEmail(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.RIDER_NOT_FOUND));
    }

    /** 理쒖떊 ?좎껌 ?곹깭 湲곗??쇰줈 ?ъ떊泥?媛???щ?瑜?寃利앺빀?덈떎. */
    private void validateReapplyAllowed(User user) {
        approvalRepository.findTopByUserAndApplicantTypeOrderByIdDesc(user, ApplicantType.RIDER)
                .ifPresent(approval -> {
                    if (approval.getStatus() == ApprovalStatus.PENDING) {
                        throw new BusinessException(ErrorCode.RIDER_APPROVAL_ALREADY_EXISTS);
                    }
                    if (approval.getStatus() == ApprovalStatus.REJECTED) {
                        throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "嫄곗젅???좎껌? ?ㅼ떆 ?쒖텧?????놁뒿?덈떎.");
                    }
                });
    }

    private void notifyAdminsForRiderSubmission(User applicant, Rider rider) {
        String title = "[신청 접수] 라이더 등록 신청";
        String content = String.format("신청자: %s (%s)", rider.getDisplayName(), applicant.getEmail());

        userRepository.findAllActiveByRoleName("ADMIN")
                .forEach(admin -> notificationService.createNotification(
                        admin.getId(),
                        title,
                        content,
                        NotificationRefType.RIDER
                ));
    }
}

