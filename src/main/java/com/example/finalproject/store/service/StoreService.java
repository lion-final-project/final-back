package com.example.finalproject.store.service;

import com.example.finalproject.global.exception.custom.BusinessException;
import com.example.finalproject.global.exception.custom.ErrorCode;
import com.example.finalproject.global.util.GeometryUtil;
import com.example.finalproject.moderation.domain.Approval;
import com.example.finalproject.moderation.enums.ApplicantType;
import com.example.finalproject.moderation.enums.ApprovalStatus;
import com.example.finalproject.moderation.enums.DocumentType;
import com.example.finalproject.moderation.repository.ApprovalRepository;
import com.example.finalproject.store.domain.embedded.SettlementAccount;
import com.example.finalproject.store.domain.Store;
import com.example.finalproject.store.domain.embedded.StoreAddress;
import com.example.finalproject.store.domain.StoreBusinessHour;
import com.example.finalproject.store.domain.embedded.SubmittedDocumentInfo;
import com.example.finalproject.store.dto.request.PostStoreBusinessHourRequest;
import com.example.finalproject.store.dto.request.PostStoreRegistrationRequest;
import com.example.finalproject.store.dto.response.GetStoreCategoryResponse;
import com.example.finalproject.store.dto.response.GetStoreRegistrationStatusResponse;
import com.example.finalproject.store.dto.response.PostStoreRegistrationResponse;
import com.example.finalproject.store.dto.response.GetMyStoreResponse;
import com.example.finalproject.store.domain.StoreCategory;
import com.example.finalproject.store.enums.StoreStatus;
import com.example.finalproject.store.repository.StoreCategoryRepository;
import com.example.finalproject.store.repository.StoreRepository;
import com.example.finalproject.user.domain.User;
import com.example.finalproject.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class StoreService {

    private final StoreRepository storeRepository;
    private final StoreCategoryRepository storeCategoryRepository;
    private final ApprovalRepository approvalRepository;
    private final UserRepository userRepository;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");



    public PostStoreRegistrationResponse createStoreApplication(String userName, PostStoreRegistrationRequest request) {

        User user = findUserByUserName(userName);

        //입점 신청 조건 확인
        validateRegistration(user, request);

        //마트 생성(PENDING 상태) + 운영 시간 추가
        Store store = createStore(user, request);
        addBusinessHours(store, request.getBusinessHours());
        Store savedStore = storeRepository.save(store);
        log.info("마트 및 운영 시간 저장 성공");

        //승인 신청 생성 + 증빙 서류 추가
        Approval approval = createApproval(user);
        addApprovalDocuments(approval, request);
        Approval savedApproval = approvalRepository.save(approval);
        log.info("마트 승인 신청 이력 저장 성공 approvalId = {}", savedApproval.getId());

        log.info("마트 입점 신청 완료. storeId={}, userId={}", savedStore.getId(), user.getId());

        return PostStoreRegistrationResponse.of(
                savedStore.getId(),
                savedApproval.getId(),
                savedStore.getStatus(),
                savedStore.getStoreName(),
                savedStore.getRepresentativeName()
        );
    }

    /**
     * 현재 사용자의 마트 입점 신청 현황 조회 (상호명 포함).
     * 신청 이력이 없으면 Optional.empty() 반환.
     */
    @Transactional(readOnly = true)
    public java.util.Optional<GetStoreRegistrationStatusResponse> getMyStoreRegistration(String userName) {
        User user = findUserByUserName(userName);
        return storeRepository.findByOwner(user)
                .map(store -> GetStoreRegistrationStatusResponse.of(
                        store.getStatus(),
                        store.getStoreName(),
                        store.getRepresentativeName()
                ));
    }

    public void cancelStoreRegistration(String userName) {
        User user = findUserByUserName(userName);

        Store store = storeRepository.findByOwner(user)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_PENDING_REGISTRATION_NOT_FOUND));

        if (store.getStatus() != StoreStatus.PENDING) {
            throw new BusinessException(ErrorCode.STORE_ALREADY_APPROVED);
        }

        Approval approval = approvalRepository.findFirstByUserAndApplicantTypeAndStatus(user, ApplicantType.STORE, ApprovalStatus.PENDING)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_PENDING_REGISTRATION_NOT_FOUND));

        approvalRepository.delete(approval);
        storeRepository.delete(store);
        log.info("마트 입점 신청 취소 완료. storeId={}, userId={}", store.getId(), user.getId());
    }

    @Transactional(readOnly = true)
    public GetMyStoreResponse getMyStore(String userName) {
        User user = userRepository.findByEmail(userName)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        Store store = storeRepository.findByOwner(user)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));
        return GetMyStoreResponse.from(store);
    }

    /** 고객(상점 없음)인 경우 null 반환. 404 대신 200 + null 로 응답할 때 사용. */
    @Transactional(readOnly = true)
    public java.util.Optional<GetMyStoreResponse> getMyStoreOptional(String userName) {
        User user = userRepository.findByEmail(userName).orElse(null);
        if (user == null) {
            return java.util.Optional.empty();
        }
        return storeRepository.findByOwner(user).map(GetMyStoreResponse::from);
    }

    @Transactional(readOnly = true)
    public List<GetStoreCategoryResponse> getAllCategories() {
        return GetStoreCategoryResponse.fromList(storeCategoryRepository.findAll());
    }

    private void validateRegistration(User user, PostStoreRegistrationRequest request) {
        if (storeRepository.existsBySubmittedDocumentInfo_BusinessNumber(request.getBusinessNumber())) {
            throw new BusinessException(ErrorCode.DUPLICATE_BUSINESS_NUMBER);
        }

        if (storeRepository.existsBySubmittedDocumentInfo_TelecomSalesReportNumber(request.getTelecomSalesReportNumber())) {
            throw new BusinessException(ErrorCode.DUPLICATE_TELECOM_SALES_NUMBER);
        }

        if (storeRepository.existsByOwner(user)) {
            throw new BusinessException(ErrorCode.ALREADY_REGISTERED_STORE);
        }

        if (approvalRepository.existsByUserAndApplicantTypeAndStatus(user, ApplicantType.STORE, ApprovalStatus.PENDING)) {
            throw new BusinessException(ErrorCode.PENDING_APPROVAL_EXISTS);
        }

        validateBusinessHours(request.getBusinessHours());
    }

    private void validateBusinessHours(List<PostStoreBusinessHourRequest> businessHours) {
        for (PostStoreBusinessHourRequest hour : businessHours) {
            if (!hour.getIsClosed()) {
                if (hour.getOpenTime() == null || hour.getOpenTime().isBlank() ||
                    hour.getCloseTime() == null || hour.getCloseTime().isBlank()) {
                    throw new BusinessException(ErrorCode.INVALID_BUSINESS_HOUR);
                }
            }
        }
    }

    private Store createStore(User user, PostStoreRegistrationRequest request) {
        StoreCategory category = storeCategoryRepository.findByCategoryName(request.getStoreCategory())
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_CATEGORY_NOT_FOUND));

        SubmittedDocumentInfo submittedDocumentInfo = SubmittedDocumentInfo.builder()
                .businessOwnerName(request.getStoreOwnerName())
                .businessNumber(request.getBusinessNumber())
                .telecomSalesReportNumber(request.getTelecomSalesReportNumber())
                .build();

        Point location = GeometryUtil.createPoint(request.getLongitude(), request.getLatitude());

        String addressLine2 = (request.getAddressLine2() != null && !request.getAddressLine2().isBlank())
                ? request.getAddressLine2() : null;
        String postalCode = (request.getPostalCode() != null && !request.getPostalCode().isBlank())
                ? request.getPostalCode() : "";
        StoreAddress address = StoreAddress.builder()
                .postalCode(postalCode)
                .addressLine1(request.getAddressLine())
                .addressLine2(addressLine2)
                .location(location)
                .build();

        SettlementAccount settlementAccount = SettlementAccount.builder()
                .bankName(request.getSettlementBankName())
                .bankAccount(request.getSettlementBankAccount())
                .accountHolder(request.getSettlementAccountHolder())
                .build();

        return Store.builder()
                .owner(user)
                .storeCategory(category)
                .storeName(request.getStoreName())
                .phone(request.getStorePhone())
                .description(request.getStoreDescription())
                .representativeName(request.getRepresentativeName())
                .representativePhone(request.getRepresentativePhone())
                .submittedDocumentInfo(submittedDocumentInfo)
                .address(address)
                .settlementAccount(settlementAccount)
                .storeImage(request.getStoreImageUrl())
                .build();
    }

    private void addBusinessHours(Store store, List<PostStoreBusinessHourRequest> postStoreBusinessHourRequests) {
        for (PostStoreBusinessHourRequest hourRequest : postStoreBusinessHourRequests) {
            LocalTime openTime = null;
            LocalTime closeTime = null;

            if (!hourRequest.getIsClosed()) {
                if (hourRequest.getOpenTime() != null && !hourRequest.getOpenTime().isEmpty()) {
                    openTime = LocalTime.parse(hourRequest.getOpenTime(), TIME_FORMATTER);
                }
                if (hourRequest.getCloseTime() != null && !hourRequest.getCloseTime().isEmpty()) {
                    closeTime = LocalTime.parse(hourRequest.getCloseTime(), TIME_FORMATTER);
                }
            }

            StoreBusinessHour businessHour = StoreBusinessHour.builder()
                    .dayOfWeek(hourRequest.getDayOfWeek())
                    .openTime(openTime)
                    .closeTime(closeTime)
                    .isClosed(hourRequest.getIsClosed())
                    .build();

            store.addBusinessHour(businessHour);
        }
    }

    private Approval createApproval(User user) {
        return Approval.builder()
                .user(user)
                .applicantType(ApplicantType.STORE)
                .build();
    }

    private void addApprovalDocuments(Approval approval, PostStoreRegistrationRequest request) {
        //사업자 등록증
        approval.addDocument(DocumentType.BUSINESS_LICENSE, request.getBusinessLicenseUrl());

        //통신판매업 신고증
        approval.addDocument(DocumentType.BUSINESS_REPORT, request.getTelecomSalesReportUrl());

        //통장 사본
        approval.addDocument(DocumentType.BANK_PASSBOOK, request.getBankPassbookUrl());
    }

    private User findUserByUserName(String username) {
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

}
