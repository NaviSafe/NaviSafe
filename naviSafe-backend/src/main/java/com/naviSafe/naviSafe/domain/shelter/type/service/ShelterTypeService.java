package com.naviSafe.naviSafe.domain.shelter.type.service;

import com.naviSafe.naviSafe.domain.shelter.type.entity.ShelterType;
import com.naviSafe.naviSafe.domain.shelter.type.repository.ShelterTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ShelterTypeService {

    private final ShelterTypeRepository shelterTypeRepository;

    @Autowired
    public ShelterTypeService(ShelterTypeRepository shelterTypeRepository) {
        this.shelterTypeRepository = shelterTypeRepository;
    }

    public Optional<ShelterType> findByShelterCode(int shelterCode) {
        return shelterTypeRepository.findByShelterCode(shelterCode);
    }
}
