package com.example.finalproject.store.repository;

import com.example.finalproject.store.domain.Store;
import java.util.List;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.finalproject.store.repository.custom.StoreRepositoryCustom;

public interface StoreRepository extends JpaRepository<Store, Long>, StoreRepositoryCustom {

}
