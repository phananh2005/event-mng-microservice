package com.sa.authservice.service;

import com.sa.authservice.repository.InvalidatedTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupService {

    private final InvalidatedTokenRepository invalidatedTokenRepository;

    @Scheduled(cron = "0 0 0 * * *")
    public void cleanupExpiredTokens() {
        invalidatedTokenRepository.deleteByExpiryTimeBefore(new Date());
        log.debug("Expired invalidated tokens cleaned up");
    }
}

