package com.ticker_now.weather_flow.domain.region.entitiy;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "REGION")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Region {
    @Id
    @Column(name = "GU_CODE")
    private int guCode;

    @Column(name = "GU_NAME")
    private String guName;
}
