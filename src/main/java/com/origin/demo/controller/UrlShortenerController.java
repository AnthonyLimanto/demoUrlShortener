package com.origin.demo.controller;

import com.origin.demo.dto.ShortenRequest;
import com.origin.demo.dto.ShortenResponse;
import com.origin.demo.model.UrlMapping;
import com.origin.demo.service.UrlShortenerService;
import com.origin.demo.utils.UrlValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@Slf4j
public class UrlShortenerController {

    @Autowired
    UrlShortenerService urlShortenerService;

//  Shorten Url
    @PostMapping("/shorten")
    public ResponseEntity<ShortenResponse> shorten(@RequestBody ShortenRequest shortenRequest) {
        String url = shortenRequest.getUrl();
        if (!UrlValidator.isValid(url)) {
            log.error("{} is an invalid Url", url);
            return ResponseEntity.badRequest().build();
        }
        String shortUrl = urlShortenerService.getShortUrl(url);

        return ResponseEntity.ok(new ShortenResponse(shortUrl));
    }

//  Redirect
    @GetMapping("/{code}")
    public ResponseEntity<Void> redirect(@PathVariable String code) {
        String originalUrl = urlShortenerService.getUrlMapping(code).getOriginalUrl();
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(originalUrl))
                .build();
    }

//  Get original url info from short url
    @GetMapping("/info/{code}")
    public ResponseEntity<UrlMapping> getInfo(@PathVariable String code) {
        return ResponseEntity.ok(urlShortenerService.getUrlMapping(code));
    }
}
