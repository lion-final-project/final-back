package com.example.finalproject.store.repository;

import com.example.finalproject.store.domain.Store;
import com.example.finalproject.store.domain.StoreBusinessHour;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoreBusinessHourRepository extends JpaRepository<StoreBusinessHour, Long> {

    Optional<StoreBusinessHour> findByStoreAndDayOfWeek(Store store, Short dayOfWeek);
}
