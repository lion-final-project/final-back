package com.example.finalproject.product.repository;

import com.example.finalproject.product.domain.Product;
import com.example.finalproject.store.domain.Store;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByIdAndStoreId(Long id, Long storeId);

    Optional<Product> findByIdAndDeletedAtIsNull(Long id);

    Page<Product> findByStoreAndDeletedAtIsNull(Store store, Pageable pageable);

    Long countByStoreAndDeletedAtIsNull(Store store);

    Long countByStoreAndIsActiveAndDeletedAtIsNull(Store store, Boolean isActive);

    boolean existsByStoreAndProductNameAndDeletedAtIsNull(Store store, String productName);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id in :ids")
    List<Product> findAllByIdForUpdate(@Param("ids") List<Long> ids);

    List<Product> findAllByIdInAndDeletedAtIsNull(List<Long> ids);


}