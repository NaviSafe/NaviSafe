package com.naviSafe.naviSafe.domain.outbreakOccur.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class OutbreakRequestDto {

    private List<String> excludeAccTypeNames;
}
