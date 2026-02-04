package com.example.finalproject.store.service;

import com.example.finalproject.global.exception.custom.BusinessException;
import com.example.finalproject.global.exception.custom.ErrorCode;
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
import com.example.finalproject.store.dto.response.StoreRegistrationResponse;
import com.example.finalproject.store.domain.StoreCategory;
import com.example.finalproject.store.repository.StoreCategoryRepository;
import com.example.finalproject.store.repository.StoreRepository;
import com.example.finalproject.user.domain.User;
import com.example.finalproject.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
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
    //todo: 좌표 관련 컨피그 만들어지거나 할 경우 교체 되어야 한다.
    private static final int SRID = 4326;



    public StoreRegistrationResponse createStoreApplication(String userName, PostStoreRegistrationRequest request) {

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

        return StoreRegistrationResponse.of(savedStore.getId(), savedApproval.getId(), savedStore.getStatus());
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
    }

    private Store createStore(User user, PostStoreRegistrationRequest request) {
        StoreCategory category = storeCategoryRepository.findByCategoryName(request.getStoreCategory())
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_CATEGORY_NOT_FOUND));

        SubmittedDocumentInfo submittedDocumentInfo = SubmittedDocumentInfo.builder()
                .businessOwnerName(request.getStoreOwnerName())
                .businessNumber(request.getBusinessNumber())
                .telecomSalesReportNumber(request.getTelecomSalesReportNumber())
                .build();

        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), SRID);
        Point location = geometryFactory.createPoint(
                new Coordinate(request.getLongitude(), request.getLatitude())
        );

        StoreAddress address = StoreAddress.builder()
                .addressLine1(request.getAddressLine())
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

    //todo: 유저서비스가 개발될건지,,? 없으면 해당 익센션 만들어야함 라이더에서도 스토어에서도 스토리지에서도 이 코드는 각기 쓰이고있음...
    private User findUserByUserName(String username) {
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

}
