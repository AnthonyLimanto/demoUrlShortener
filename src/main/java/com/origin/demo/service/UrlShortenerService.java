package com.origin.demo.service;

import com.origin.demo.exception.NotFoundException;
import com.origin.demo.model.UrlMapping;
import com.origin.demo.repository.UrlMappingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UrlShortenerService {

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
                log.warn("Collision detected on attempt {}: shortCode='{}'. Retrying... ", attempt, code);
            } catch (Exception e) {
                log.error("Unexpected database error while saving URL mapping: {}", e.getMessage(), e);
            }
        }

        throw new IllegalStateException(
                "Failed to generate unique code after " + MAX_RETRIES + " attempts");
    }

    public UrlMapping getUrlMapping(String shortCode) {
        Optional<UrlMapping> urlMapping = urlMappingRepository.findByShortCode(shortCode);
        if (!urlMapping.isPresent()) {
            throw new NotFoundException("Short Url not found");
        }
        return urlMapping.get();
    }


    // If we want to change the logic for generating shortUrl we can just change this
    private String generateCode() {
        return RandomStringUtils.randomAlphanumeric(CODE_LENGTH);
    }
}
