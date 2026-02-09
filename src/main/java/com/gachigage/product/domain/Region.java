package com.gachigage.product.domain;

import com.gachigage.global.error.CustomException;
import com.gachigage.global.error.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "region")
public class Region {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "province", length = 50, nullable = false)
    private String province; // 도

    @Column(name = "city", length = 50, nullable = false)
    private String city; // 시

    @Column(name = "district", length = 50)
    private String district; // 구

    @Column(name = "code", length = 10)
    private String lawCode; // 도, 시/구에 대한 코드는 앞 5자리 고정, 동/읍/면에 대한 코드는 뒤 5자리 고정

    @Builder
    public Region(String province, String city, String district, String lawCode) {
        this.province = province;
        this.city = city;
        this.district = district;
        this.lawCode = lawCode;

        validateLawCodeLength(lawCode);
    }

    private void validateLawCodeLength(String lawCode) {
        if (this.lawCode != null && this.lawCode.length() != 10) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "법정동 코드는 10자리여야 합니다.");
        }
    }
}
