package com.example.finalproject.store.domain;

import com.example.finalproject.store.enums.StoreCategoryType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "store_categories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "category_name", nullable = false, unique = true, length = 50)
    private StoreCategoryType categoryName;

    @Builder
    public StoreCategory(StoreCategoryType categoryName) {
        this.categoryName = categoryName;
    }
}
