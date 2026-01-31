package com.example.finalproject.delivery.service;


import com.example.finalproject.delivery.domain.Rider;
import com.example.finalproject.delivery.dto.request.PatchRiderStatusRequest;
import com.example.finalproject.delivery.dto.response.RiderResponse;
import com.example.finalproject.delivery.enums.RiderOperationStatus;
import com.example.finalproject.delivery.repository.RiderRepository;
import com.example.finalproject.delivery.service.interfaces.RiderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RiderServiceImpl implements RiderService {
    private  final RiderRepository riderRepository;

    @Override
    @Transactional
    public RiderResponse updateOperationStatus(String username, PatchRiderStatusRequest request) {

        Rider rider = findRiderByUsername(username);
        rider.setOperationStatus(request.getOperationStatus());
        riderRepository.save(rider);

        return RiderResponse.from(rider);
    }



    private Rider findRiderByUsername(String username) {
        return riderRepository.findByUserEmail(username)
                .orElseThrow(() -> new RuntimeException("Rider not found"));
    }
}
