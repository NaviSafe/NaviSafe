package com.naviSafe.naviSafe.domain.rain.entitiy;

import com.naviSafe.naviSafe.domain.region.entitiy.Region;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "RAIN")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Rain {
    @Id
    @Column(name = "GU_CODE")
    private int guCode;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "GU_CODE")
    private Region region;

    @Column(name = "RAINFALL10")
    private String rainFall10;

    @Column(name = "RECEIVE_TIME")
    private String receiveTime;
}
