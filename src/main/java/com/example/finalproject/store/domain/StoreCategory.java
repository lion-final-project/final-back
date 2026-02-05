package com.example.finalproject.store.domain;

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

    @Column(name = "category_name", nullable = false, unique = true, length = 50)
    private String categoryName;

    @Builder
    public StoreCategory(String categoryName) {
        this.categoryName = categoryName;
    }
}
