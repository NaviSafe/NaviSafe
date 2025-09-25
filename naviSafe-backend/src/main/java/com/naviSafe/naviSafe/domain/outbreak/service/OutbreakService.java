package com.naviSafe.naviSafe.domain.outbreak.service;

import com.naviSafe.naviSafe.domain.outbreak.entitiy.Outbreak;
import com.naviSafe.naviSafe.domain.outbreak.repository.OutbreakRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class OutbreakService {
    private final OutbreakRepository outbreakRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String KEY = "outbreak:all";

    @Autowired
    public OutbreakService(OutbreakRepository outbreakRepository, RedisTemplate<String, Object> redisTemplate) {
        this.outbreakRepository = outbreakRepository;
        this.redisTemplate = redisTemplate;
    }

    public List<Outbreak> findAll() {

        List<Outbreak> cached = (List<Outbreak>) redisTemplate.opsForValue().get(KEY);
        if(cached != null) {
            return cached;
        }

        List<Outbreak> outbreaks = outbreakRepository.findAll();

        redisTemplate.opsForValue().set(KEY, outbreaks, 30, TimeUnit.MINUTES);

        return outbreaks;
    }
}
