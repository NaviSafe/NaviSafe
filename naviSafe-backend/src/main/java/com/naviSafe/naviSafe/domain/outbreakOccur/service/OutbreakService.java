package com.naviSafe.naviSafe.domain.outbreakOccur.service;

import com.naviSafe.naviSafe.domain.outbreakOccur.entity.OutbreakOccur;
import com.naviSafe.naviSafe.domain.outbreakOccur.repository.OutbreakRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

        ZoneId koreaZone = ZoneId.of("Asia/Seoul");
        ZonedDateTime nowInKorea = ZonedDateTime.now(koreaZone);

        List<OutbreakOccur> validOutbreaks = outbreaks.stream()
                .filter(outbreak -> {
                    ZonedDateTime expDate = outbreak.getExpClrDate();
                    return expDate == null || expDate.isAfter(nowInKorea);
                })
                .toList();

        boolean hasNull = outbreaks.stream().anyMatch(o ->
                o.getRoadStatusLink().getLinkId() == null ||
                        o.getRoadStatusLink().getRoadStatus() == null ||
                        o.getRoadStatusLink().getOutbreakAccId() == null
        );

        if (!hasNull) {
            redisTemplate.opsForValue().set(KEY, validOutbreaks, 1, TimeUnit.MINUTES);
        }
        return validOutbreaks;
    }
}
