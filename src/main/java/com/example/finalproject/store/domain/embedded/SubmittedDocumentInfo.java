package com.example.finalproject.store.domain.embedded;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SubmittedDocumentInfo {

    //사업자명 -> ddl 에 없던거임
    @Column(name = "business_owner_name", nullable = false, length = 50)
    private String businessOwnerName;

    //사업자 등록번호
    @Column(name = "business_number", nullable = false, unique = true, length = 12)
    private String businessNumber;

    //통신판매업 신고번호
    @Column(name = "telecom_sales_report_number", nullable = false, unique = true, length = 50)
    private String telecomSalesReportNumber;

    @Builder
    public SubmittedDocumentInfo(String businessOwnerName, String businessNumber, String telecomSalesReportNumber) {
        this.businessOwnerName = businessOwnerName;
        this.businessNumber = businessNumber;
        this.telecomSalesReportNumber = telecomSalesReportNumber;
    }
}
