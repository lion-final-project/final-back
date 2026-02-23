package com.example.finalproject.store.service;

import com.example.finalproject.global.exception.custom.BusinessException;
import com.example.finalproject.global.exception.custom.ErrorCode;
import com.example.finalproject.global.util.GeometryUtil;
import com.example.finalproject.communication.enums.NotificationRefType;
import com.example.finalproject.communication.service.NotificationService;
import com.example.finalproject.moderation.domain.Approval;
import com.example.finalproject.moderation.domain.ApprovalDocument;
import com.example.finalproject.moderation.enums.ApplicantType;
import com.example.finalproject.moderation.enums.ApprovalStatus;
import com.example.finalproject.moderation.enums.DocumentType;
import com.example.finalproject.moderation.repository.ApprovalDocumentRepository;
import com.example.finalproject.moderation.repository.ApprovalRepository;
import com.example.finalproject.store.domain.Store;
import com.example.finalproject.store.domain.StoreBusinessHour;
import com.example.finalproject.store.domain.StoreCategory;
import com.example.finalproject.store.domain.embedded.SettlementAccount;
import com.example.finalproject.store.domain.embedded.StoreAddress;
import com.example.finalproject.store.domain.embedded.SubmittedDocumentInfo;
import com.example.finalproject.store.dto.request.PatchDeliveryAvailableRequest;
import com.example.finalproject.store.dto.request.PatchStoreDescriptionRequest;
import com.example.finalproject.store.dto.request.PatchStoreImageRequest;
import com.example.finalproject.store.dto.request.PostStoreBusinessHourRequest;
import com.example.finalproject.store.dto.request.PostStoreRegistrationRequest;
import com.example.finalproject.store.dto.response.GetMyStoreResponse;
import com.example.finalproject.store.dto.response.GetStoreCategoryResponse;
import com.example.finalproject.store.dto.response.GetStoreRegistrationDetailResponse;
import com.example.finalproject.store.dto.response.GetStoreDetailForCustomerResponse;
import com.example.finalproject.store.dto.response.GetStoreRegistrationStatusResponse;
import com.example.finalproject.store.dto.response.PostStoreRegistrationResponse;
import com.example.finalproject.store.enums.StoreStatus;
import com.example.finalproject.store.repository.StoreCategoryRepository;
import com.example.finalproject.store.repository.StoreRepository;
import com.example.finalproject.user.domain.User;
import com.example.finalproject.user.repository.UserRepository;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class StoreService {

    private final StoreRepository storeRepository;
    private final StoreCategoryRepository storeCategoryRepository;
    private final ApprovalRepository approvalRepository;
    private final ApprovalDocumentRepository approvalDocumentRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public PostStoreRegistrationResponse createStoreApplication(String userName, PostStoreRegistrationRequest request) {
        User user = findUserByUserName(userName);
        Optional<Store> existingStore = storeRepository.findByOwner(user);
        Optional<Approval> latestApproval = approvalRepository.findTopByUserAndApplicantTypeOrderByIdDesc(
                user, ApplicantType.STORE);

        validateRegistration(user, request, existingStore.orElse(null));
        Store store;
        if (existingStore.isPresent() && existingStore.get().getStatus() != StoreStatus.APPROVED) {
            // 재신청 시 기존 스토어를 삭제하지 않고 갱신한다. (products FK 충돌 방지)
            store = existingStore.get();
            updateStoreForRegistration(store, request);
            store.getBusinessHours().clear();
            storeRepository.saveAndFlush(store);
            addBusinessHours(store, request.getBusinessHours());
        } else {
            store = createStore(user, request);
            addBusinessHours(store, request.getBusinessHours());
        }
        Store savedStore = storeRepository.save(store);

        Approval approval;
        if (latestApproval.isPresent() && latestApproval.get().getStatus() == ApprovalStatus.HELD) {
            approval = latestApproval.get();
            approval.resubmit();
            approval.clearDocuments();
            // 기존 문서 삭제를 먼저 flush 해서 문서 재업로드 시 유니크 충돌을 방지한다.
            approvalRepository.saveAndFlush(approval);
        } else {
            approval = createApproval(user);
        }
        addApprovalDocuments(approval, request);
        Approval savedApproval = approvalRepository.save(approval);
        notifyAdminsForStoreSubmission(user, savedStore);

        return PostStoreRegistrationResponse.of(
                savedStore.getId(),
                savedApproval.getId(),
                savedStore.getStatus(),
                savedStore.getStoreName(),
                savedStore.getRepresentativeName()
        );
    }

    @Transactional(readOnly = true)
    public Optional<GetStoreRegistrationStatusResponse> getMyStoreRegistration(String userName) {
        User user = findUserByUserName(userName);
        Optional<Store> storeOptional = storeRepository.findByOwner(user);
        Optional<Approval> latestApproval = approvalRepository.findTopByUserAndApplicantTypeOrderByIdDesc(user, ApplicantType.STORE);

        if (latestApproval.isPresent()) {
            Approval approval = latestApproval.get();
            return Optional.of(GetStoreRegistrationStatusResponse.of(
                    approval.getStatus().name(),
                    storeOptional.map(Store::getStoreName).orElse(null),
                    storeOptional.map(Store::getRepresentativeName).orElse(null),
                    approval.getId(),
                    approval.getReason(),
                    approval.getHeldUntil()
            ));
        }

        return storeOptional.map(store -> GetStoreRegistrationStatusResponse.of(
                store.getStatus(),
                store.getStoreName(),
                store.getRepresentativeName(),
                null,
                null,
                null
        ));
    }

    @Transactional(readOnly = true)
    public Optional<GetStoreRegistrationDetailResponse> getMyStoreRegistrationDetail(String userName) {
        User user = findUserByUserName(userName);
        Optional<Store> storeOptional = storeRepository.findByOwner(user);
        Optional<Approval> latestApproval = approvalRepository.findTopByUserAndApplicantTypeOrderByIdDesc(user, ApplicantType.STORE);

        if (storeOptional.isEmpty()) {
            return Optional.empty();
        }
        Store store = storeOptional.get();

        Long approvalId = latestApproval.map(Approval::getId).orElse(null);
        Map<String, String> documents = Map.of();
        String status = store.getStatus().name();
        String reason = null;
        java.time.LocalDateTime heldUntil = null;

        if (latestApproval.isPresent()) {
            Approval approval = latestApproval.get();
            status = approval.getStatus().name();
            reason = approval.getReason();
            heldUntil = approval.getHeldUntil();
            documents = approvalDocumentRepository.findByApprovalId(approval.getId()).stream()
                    .collect(Collectors.toMap(
                            doc -> doc.getDocumentType().name(),
                            ApprovalDocument::getDocumentUrl,
                            (a, b) -> b
                    ));
        }

        Double latitude = null;
        Double longitude = null;
        if (store.getAddress() != null && store.getAddress().getLocation() != null) {
            longitude = store.getAddress().getLocation().getX();
            latitude = store.getAddress().getLocation().getY();
        }

        return Optional.of(GetStoreRegistrationDetailResponse.builder()
                .status(status)
                .approvalId(approvalId)
                .reason(reason)
                .heldUntil(heldUntil)
                .storeCategory(store.getStoreCategory() != null ? store.getStoreCategory().getCategoryName() : null)
                .storeOwnerName(store.getSubmittedDocumentInfo() != null ? store.getSubmittedDocumentInfo().getBusinessOwnerName() : null)
                .storeName(store.getStoreName())
                .representativeName(store.getRepresentativeName())
                .representativePhone(store.getRepresentativePhone())
                .storePhone(store.getPhone())
                .storeDescription(store.getDescription())
                .storeImageUrl(store.getStoreImage())
                .businessNumber(store.getSubmittedDocumentInfo() != null ? store.getSubmittedDocumentInfo().getBusinessNumber() : null)
                .telecomSalesReportNumber(store.getSubmittedDocumentInfo() != null ? store.getSubmittedDocumentInfo().getTelecomSalesReportNumber() : null)
                .postalCode(store.getAddress() != null ? store.getAddress().getPostalCode() : null)
                .addressLine1(store.getAddress() != null ? store.getAddress().getAddressLine1() : null)
                .addressLine2(store.getAddress() != null ? store.getAddress().getAddressLine2() : null)
                .latitude(latitude)
                .longitude(longitude)
                .settlementBankName(store.getSettlementAccount() != null ? store.getSettlementAccount().getBankName() : null)
                .settlementBankAccount(store.getSettlementAccount() != null ? store.getSettlementAccount().getBankAccount() : null)
                .settlementAccountHolder(store.getSettlementAccount() != null ? store.getSettlementAccount().getAccountHolder() : null)
                .documents(documents)
                .build());
    }

    public void cancelStoreRegistration(String userName) {
        User user = findUserByUserName(userName);

        Store store = storeRepository.findByOwner(user)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_PENDING_REGISTRATION_NOT_FOUND));

        if (store.getStatus() == StoreStatus.APPROVED) {
            throw new BusinessException(ErrorCode.STORE_ALREADY_APPROVED);
        }

        approvalRepository.findTopByUserAndApplicantTypeOrderByIdDesc(user, ApplicantType.STORE)
                .ifPresent(approval -> {
                    if (approval.getStatus() == ApprovalStatus.PENDING
                            || approval.getStatus() == ApprovalStatus.HELD
                            || approval.getStatus() == ApprovalStatus.REJECTED) {
                        approvalRepository.delete(approval);
                    }
                });

        storeRepository.delete(store);
    }

    @Transactional(readOnly = true)
    public GetMyStoreResponse getMyStore(String userName) {
        User user = userRepository.findByEmail(userName)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        Store store = storeRepository.findByOwner(user)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));
        return GetMyStoreResponse.from(store);
    }

    @Transactional(readOnly = true)
    public Optional<GetMyStoreResponse> getMyStoreOptional(String userName) {
        User user = userRepository.findByEmail(userName).orElse(null);
        if (user == null) {
            return Optional.empty();
        }
        return storeRepository.findByOwner(user).map(GetMyStoreResponse::from);
    }

    @Transactional(readOnly = true)
    public List<GetStoreCategoryResponse> getAllCategories() {
        return GetStoreCategoryResponse.fromList(storeCategoryRepository.findAll());
    }

    @Transactional(readOnly = true)
    public Optional<GetStoreDetailForCustomerResponse> getStoreDetailForCustomer(Long storeId) {
        if (storeId == null) return Optional.empty();
        return storeRepository.findById(storeId)
                .filter(s -> s.getStatus() == StoreStatus.APPROVED && s.getDeletedAt() == null)
                .map(GetStoreDetailForCustomerResponse::from);
    }

    @Transactional(readOnly = true)
    public List<PostStoreBusinessHourRequest> getStoreBusinessHours(String userName) {
        User user = findUserByUserName(userName);
        Store store = storeRepository.findByOwner(user)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));

        return store.getBusinessHours().stream()
                .sorted(java.util.Comparator.comparing(StoreBusinessHour::getDayOfWeek))
                .map(this::toBusinessHourRequest)
                .toList();
    }

    public void updateStoreBusinessHours(String userName, List<PostStoreBusinessHourRequest> businessHours) {
        User user = findUserByUserName(userName);
        Store store = storeRepository.findByOwner(user)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));

        if (Boolean.TRUE.equals(store.getIsDeliveryAvailable())) {
            throw new BusinessException(ErrorCode.STORE_BUSINESS_HOUR_UPDATE_NOT_ALLOWED);
        }

        validateBusinessHours(businessHours);
        updateExistingBusinessHours(store, businessHours);
    }

    public void updateDeliveryAvailable(String userName, PatchDeliveryAvailableRequest request) {
        User user = findUserByUserName(userName);
        Store store = storeRepository.findByOwner(user)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));
        store.setDeliveryAvailable(Boolean.TRUE.equals(request.getDeliveryAvailable()));
    }

    public void updateStoreImage(String userName, PatchStoreImageRequest request) {
        User user = findUserByUserName(userName);
        Store store = storeRepository.findByOwner(user)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));
        String url = request.getStoreImageUrl() != null ? request.getStoreImageUrl() : "";
        store.updateStoreImage(url);
    }

    public void updateStoreDescription(String userName, PatchStoreDescriptionRequest request) {
        User user = findUserByUserName(userName);
        Store store = storeRepository.findByOwner(user)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));
        store.updateDescription(request.getDescription() != null ? request.getDescription() : "");
    }

    private void updateExistingBusinessHours(Store store, List<PostStoreBusinessHourRequest> businessHours) {
        Map<Short, StoreBusinessHour> byDay = store.getBusinessHours().stream()
                .collect(Collectors.toMap(StoreBusinessHour::getDayOfWeek, bh -> bh));

        for (PostStoreBusinessHourRequest req : businessHours) {
            LocalTime openTime = null;
            LocalTime closeTime = null;
            if (!req.getIsClosed()) {
                if (req.getOpenTime() != null && !req.getOpenTime().isEmpty()) {
                    openTime = LocalTime.parse(req.getOpenTime(), TIME_FORMATTER);
                }
                if (req.getCloseTime() != null && !req.getCloseTime().isEmpty()) {
                    closeTime = LocalTime.parse(req.getCloseTime(), TIME_FORMATTER);
                }
            }

            StoreBusinessHour existing = byDay.get(req.getDayOfWeek());
            if (existing != null) {
                existing.update(openTime, closeTime, req.getIsClosed());
            } else {
                StoreBusinessHour newOne = StoreBusinessHour.builder()
                        .dayOfWeek(req.getDayOfWeek())
                        .openTime(openTime)
                        .closeTime(closeTime)
                        .isClosed(req.getIsClosed())
                        .build();
                store.addBusinessHour(newOne);
            }
        }
    }

    private void validateRegistration(User user, PostStoreRegistrationRequest request, Store existingStore) {
        Long existingStoreId = existingStore != null ? existingStore.getId() : null;

        boolean duplicatedBusinessNumber = existingStoreId == null
                ? storeRepository.existsBySubmittedDocumentInfo_BusinessNumber(request.getBusinessNumber())
                : storeRepository.existsBySubmittedDocumentInfo_BusinessNumberAndIdNot(request.getBusinessNumber(), existingStoreId);
        if (duplicatedBusinessNumber) {
            throw new BusinessException(ErrorCode.DUPLICATE_BUSINESS_NUMBER);
        }

        boolean duplicatedTelecomNumber = existingStoreId == null
                ? storeRepository.existsBySubmittedDocumentInfo_TelecomSalesReportNumber(request.getTelecomSalesReportNumber())
                : storeRepository.existsBySubmittedDocumentInfo_TelecomSalesReportNumberAndIdNot(request.getTelecomSalesReportNumber(), existingStoreId);
        if (duplicatedTelecomNumber) {
            throw new BusinessException(ErrorCode.DUPLICATE_TELECOM_SALES_NUMBER);
        }

        if (existingStore != null && existingStore.getStatus() == StoreStatus.APPROVED) {
            throw new BusinessException(ErrorCode.ALREADY_REGISTERED_STORE);
        }

        approvalRepository.findTopByUserAndApplicantTypeOrderByIdDesc(user, ApplicantType.STORE)
                .ifPresent(approval -> {
                    if (approval.getStatus() == ApprovalStatus.PENDING) {
                        throw new BusinessException(ErrorCode.PENDING_APPROVAL_EXISTS);
                    }
                    if (approval.getStatus() == ApprovalStatus.REJECTED) {
                        throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                                "거절된 신청은 다시 제출할 수 없습니다.");
                    }
                });

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

    private void updateStoreForRegistration(Store store, PostStoreRegistrationRequest request) {
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

        store.replaceRegistrationInfo(
                category,
                request.getStoreName(),
                request.getStorePhone(),
                request.getStoreDescription(),
                request.getRepresentativeName(),
                request.getRepresentativePhone(),
                submittedDocumentInfo,
                address,
                settlementAccount,
                request.getStoreImageUrl()
        );
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

    private PostStoreBusinessHourRequest toBusinessHourRequest(StoreBusinessHour businessHour) {
        PostStoreBusinessHourRequest response = new PostStoreBusinessHourRequest();
        response.setDayOfWeek(businessHour.getDayOfWeek());
        response.setIsClosed(businessHour.getIsClosed());
        response.setOpenTime(businessHour.getOpenTime() != null ? businessHour.getOpenTime().format(TIME_FORMATTER) : null);
        response.setCloseTime(businessHour.getCloseTime() != null ? businessHour.getCloseTime().format(TIME_FORMATTER) : null);
        return response;
    }

    private Approval createApproval(User user) {
        return Approval.builder()
                .user(user)
                .applicantType(ApplicantType.STORE)
                .build();
    }

    private void addApprovalDocuments(Approval approval, PostStoreRegistrationRequest request) {
        approval.addDocument(DocumentType.BUSINESS_LICENSE, request.getBusinessLicenseUrl());
        approval.addDocument(DocumentType.BUSINESS_REPORT, request.getTelecomSalesReportUrl());
        approval.addDocument(DocumentType.BANK_PASSBOOK, request.getBankPassbookUrl());
    }

    private User findUserByUserName(String username) {
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private void notifyAdminsForStoreSubmission(User applicant, Store store) {
        String title = "[신청 접수] 마트 입점 신청";
        String content = String.format("신청 상호명: %s (%s)", store.getStoreName(), applicant.getEmail());

        userRepository.findAllActiveByRoleName("ADMIN")
                .forEach(admin -> notificationService.createNotification(
                        admin.getId(),
                        title,
                        content,
                        NotificationRefType.STORE
                ));
    }
}
