package com.naviSafe.naviSafe.domain.shelter.type.entity;

import com.naviSafe.naviSafe.domain.shelter.gps.entity.ShelterGps;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "SHELTER_TYPE")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShelterType {
    @Id
    @Column(name = "SHELTER_CODE")
    private int shelterCode;

    @Column(name = "SHELTER_CODE_NAME")
    private String shelterCodeName;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "SHELTER_CODE", insertable = false, updatable = false)
    private List<ShelterGps> shelterGpsList;
}
