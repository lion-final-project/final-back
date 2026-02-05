package com.example.finalproject.delivery.service;


import com.example.finalproject.delivery.domain.Rider;
import com.example.finalproject.delivery.dto.request.PatchRiderStatusRequest;
import com.example.finalproject.delivery.dto.request.PostRiderRegisterRequest;
import com.example.finalproject.delivery.dto.response.RiderApprovalResponse;
import com.example.finalproject.delivery.dto.response.RiderResponse;
import com.example.finalproject.delivery.enums.RiderOperationStatus;
import com.example.finalproject.delivery.repository.RiderRepository;
import com.example.finalproject.delivery.service.interfaces.RiderService;
import com.example.finalproject.moderation.domain.Approval;
import com.example.finalproject.moderation.enums.ApplicantType;
import com.example.finalproject.moderation.enums.ApprovalStatus;
import com.example.finalproject.moderation.enums.DocumentType;
import com.example.finalproject.moderation.repository.ApprovalRepository;
import com.example.finalproject.user.domain.User;
import com.example.finalproject.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RiderServiceImpl implements RiderService {
    private final RiderRepository riderRepository;
    private final UserRepository userRepository;
    private final ApprovalRepository approvalRepository;

    @Override
    public RiderResponse getRiderInfo(String username) {
        Rider rider = findRiderByUsername(username);
        return rider.createResponse();
    }

    @Override
    @Transactional
    public RiderResponse updateOperationStatus(String username, PatchRiderStatusRequest request) {

        Rider rider = findRiderByUsername(username);
        if (rider.getOperationStatus() == RiderOperationStatus.DELIVERING){
            throw new RuntimeException("Status locked: DELIVERING");
        }

        rider.setOperationStatus(request.getOperationStatus());
        riderRepository.save(rider);

        return rider.createResponse();
    }

    @Override
    @Transactional
    public RiderApprovalResponse createApproval(String username, PostRiderRegisterRequest request) {
        User user = findUserByUserName(username);
        Rider rider;

        if (riderRepository.existsByUserId(user.getId())){
            rider = findRiderByUsername(username);
        }else{
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
        User user = findUserByUserName(username);
        if (!riderRepository.existsByUserId(user.getId())){
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

        if (!(approval.getStatus() == ApprovalStatus.PENDING)){
            throw new RuntimeException("Approval status is not PENDING");
        }

        approvalRepository.delete(approval);
    }

    // 라이더 조회
    private Rider findRiderByUsername(String username) {
        return riderRepository.findByUserEmail(username)
                .orElseThrow(() -> new RuntimeException("Rider not found"));
    }

    // 유저 조회
    private User findUserByUserName(String username) {
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    //신청이력 삭제 가능여부 확인
    private void validateAlreadyPending(User user) throws RuntimeException{

         if (approvalRepository.existsByUserAndStatus(user, ApprovalStatus.PENDING) ||
                 approvalRepository.existsByUserAndStatus(user, ApprovalStatus.HELD)
         ){
             throw new RuntimeException("Cannot delete: Not PENDING or HELD");
         }
    }
}
