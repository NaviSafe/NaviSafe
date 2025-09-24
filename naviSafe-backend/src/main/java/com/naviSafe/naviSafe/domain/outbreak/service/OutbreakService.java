package com.naviSafe.naviSafe.domain.outbreak.service;

import com.naviSafe.naviSafe.domain.outbreak.entitiy.Outbreak;
import com.naviSafe.naviSafe.domain.outbreak.repository.OutbreakRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OutbreakService {
    private final OutbreakRepository outbreakRepository;

    @Autowired
    public OutbreakService(OutbreakRepository outbreakRepository) {
        this.outbreakRepository = outbreakRepository;
    }

    public List<Outbreak> findAll() {
        return outbreakRepository.findAll();
    }
}
