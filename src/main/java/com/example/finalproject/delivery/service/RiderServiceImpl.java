package com.example.finalproject.delivery.service;


import com.example.finalproject.delivery.domain.Rider;
import com.example.finalproject.delivery.dto.request.PatchRiderStatusRequest;
import com.example.finalproject.delivery.dto.request.PostRiderRegisterRequest;
import com.example.finalproject.delivery.dto.response.RiderApprovalResponse;
import com.example.finalproject.delivery.dto.response.RiderResponse;
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
    @Transactional
    public RiderResponse updateOperationStatus(String username, PatchRiderStatusRequest request) {

        Rider rider = findRiderByUsername(username);
        rider.setOperationStatus(request.getOperationStatus());
        riderRepository.save(rider);

        return rider.createResponse();
    }

    @Override
    @Transactional
    public RiderApprovalResponse createApproval(String username, PostRiderRegisterRequest request) {
        User user = findUserByUserName(username);
        validateAlreadyPending(user);

        Rider rider = Rider.builder()
                .user(user)
                .accountHolder(request.getAccountHolder())
                .bankAccount(request.getBankAccount())
                .bankName(request.getBankName())
                .build();

        Approval approval = Approval.builder()
                .user(user)
                .applicantType(ApplicantType.RIDER)
                .build();

        approval.addDocument(DocumentType.ID_CARD, request.getIdCardImage());
        approval.addDocument(DocumentType.BANK_PASSBOOK, request.getBankbookImage());

        riderRepository.save(rider);
        approvalRepository.save(approval);

        return approval.createResponse(rider);
    }

    private Rider findRiderByUsername(String username) {
        return riderRepository.findByUserEmail(username)
                .orElseThrow(() -> new RuntimeException("Rider not found"));
    }

    private User findUserByUserName(String username) {
        return userRepository.findUserByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private void validateAlreadyPending(User user) throws RuntimeException{

         if (approvalRepository.existsByUserAndStatus(user, ApprovalStatus.PENDING) ||
                 approvalRepository.existsByUserAndStatus(user, ApprovalStatus.APPROVED)
         ){
             throw new RuntimeException("Already Pending");
         }
    }
}
