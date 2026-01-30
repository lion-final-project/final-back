package com.example.finalproject.user.domain;

import com.example.finalproject.global.domain.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

@Entity
@Table(name = "addresses",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_addresses_user_address",
                        columnNames = {"user_id", "address_line1", "address_line2"}),
                @UniqueConstraint(name = "uq_addresses_user_name",
                        columnNames = {"user_id", "address_name"})
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Address extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_addresses_user"))
    private User user;

    @Column(nullable = false, length = 20)
    private String contact;

    @Column(name = "address_name", nullable = false, length = 50)
    private String addressName;

    @Column(name = "postal_code", nullable = false, length = 10)
    private String postalCode;

    @Column(name = "address_line1", nullable = false, length = 255)
    private String addressLine1;

    @Column(name = "address_line2", length = 255)
    private String addressLine2;

    @Column(columnDefinition = "GEOGRAPHY(POINT,4326)")
    private Point location;

    @Column(nullable = false)
    private Boolean isDefault = false;

    @Builder
    public Address(User user, String contact, String addressName,
                   String postalCode, String addressLine1, String addressLine2,
                   Point location, Boolean isDefault) {
        this.user = user;
        this.contact = contact;
        this.addressName = addressName;
        this.postalCode = postalCode;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.location = location;
        this.isDefault = isDefault != null ? isDefault : false;
    }

}
