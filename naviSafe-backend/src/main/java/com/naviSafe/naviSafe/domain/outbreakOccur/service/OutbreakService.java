package com.naviSafe.naviSafe.domain.outbreakOccur.service;

import com.naviSafe.naviSafe.domain.outbreakOccur.entity.OutbreakOccur;
import com.naviSafe.naviSafe.domain.outbreakOccur.repository.OutbreakRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

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

    public List<OutbreakOccur> findAll() {

        List<OutbreakOccur> cached = (List<OutbreakOccur>) redisTemplate.opsForValue().get(KEY);
        if(cached != null) {
            return cached;
        }

        List<OutbreakOccur> outbreaks = outbreakRepository.findAll();

        redisTemplate.opsForValue().set(KEY, outbreaks, 30, TimeUnit.MINUTES);

        return outbreaks;
    }
}
