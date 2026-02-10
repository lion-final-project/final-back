package com.example.finalproject.store.repository;

import com.example.finalproject.store.domain.Store;
import com.example.finalproject.store.enums.StoreActiveStatus;
import com.example.finalproject.store.repository.custom.StoreRepositoryCustom;
import com.example.finalproject.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long>, StoreRepositoryCustom {
    Optional<Store> findByOwnerId(Long ownerId);

    boolean existsBySubmittedDocumentInfo_BusinessNumber(String businessNumber);

    boolean existsBySubmittedDocumentInfo_TelecomSalesReportNumber(String telecomSalesReportNumber);

    boolean existsByOwner(User owner);

    Optional<Store> findByOwner(User owner);

    Page<Store> findByStoreNameContainingIgnoreCase(String storeName, Pageable pageable);

    Page<Store> findByIsActive(StoreActiveStatus isActive, Pageable pageable);

    Page<Store> findByStoreNameContainingIgnoreCaseAndIsActive(String storeName, StoreActiveStatus isActive, Pageable pageable);

    long countByIsActive(StoreActiveStatus isActive);

}
