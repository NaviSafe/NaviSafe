package com.naviSafe.naviSafe.domain.shelter.gps.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "SHELTER_GPS")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ShelterGps {

    @Id
    @Column(name = "SHELTER_NAME")
    private String shelterName;

    @Column(name = "SHELTER_ADDRESS")
    private String shelterAddress;

    @Column(name = "LOT")
    private double lot;

    @Column(name = "LAT")
    private double lat;

    @Column(name = "SHELTER_CODE")
    private int shelterCode;
}
