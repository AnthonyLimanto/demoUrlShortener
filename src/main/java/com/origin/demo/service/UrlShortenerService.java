package com.origin.demo.service;

import com.origin.demo.exception.NotFoundException;
import com.origin.demo.model.UrlMapping;
import com.origin.demo.repository.UrlMappingRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UrlShortenerService {

    private static final Logger logger = LoggerFactory.getLogger(UrlShortenerService.class);
    private final SecureRandom random = new SecureRandom();
    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int    CODE_LENGTH = 6;
    private static final int    MAX_RETRIES = 5;

    @Value("${shortener.base-url}")
    private String baseUrl;

    private final UrlMappingRepository urlMappingRepository;

    public String getShortUrl(String originalUrl) {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            String code = generateCode();
            UrlMapping urlMapping = UrlMapping.builder()
                    .shortCode(code)
                    .originalUrl(originalUrl)
                    .build();
            try {
                urlMappingRepository.save(urlMapping);
                return baseUrl + "/" + code;
            } catch (DataIntegrityViolationException e) {
                logger.warn("Collision detected on attempt {}: shortCode='{}'. Retrying... ", attempt, code);
            }
        }

        throw new IllegalStateException(
                "Failed to generate unique code after " + MAX_RETRIES + " attempts");
    }

    public UrlMapping getUrlMapping(String shortCode) {
        Optional<UrlMapping> urlMapping = urlMappingRepository.findByShortCode(shortCode);
        if (!urlMapping.isPresent()) {
            throw new NotFoundException("Short code not found");
        }
        return urlMapping.get();
    }


    // If we want to change the logic for generating shortUrl we can just change this
    private String generateCode() {
        return RandomStringUtils.randomAlphanumeric(CODE_LENGTH);
    }
}
