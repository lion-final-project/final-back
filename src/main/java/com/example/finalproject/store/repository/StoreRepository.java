package com.example.finalproject.store.repository;

import com.example.finalproject.store.domain.Store;
import com.example.finalproject.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.finalproject.store.repository.custom.StoreRepositoryCustom;
import java.util.Optional;
import org.springframework.stereotype.Repository;
@Repository
public interface StoreRepository extends JpaRepository<Store, Long>, StoreRepositoryCustom {
    Optional<Store> findByOwnerId(Long ownerId);

    boolean existsBySubmittedDocumentInfo_BusinessNumber(String businessNumber);

    boolean existsBySubmittedDocumentInfo_TelecomSalesReportNumber(String telecomSalesReportNumber);

    boolean existsByOwner(User owner);

    Optional<Store> findByOwner(User owner);
}
