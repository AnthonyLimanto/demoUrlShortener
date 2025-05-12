package com.origin.demo.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.origin.demo.dto.ShortenRequest;
import com.origin.demo.model.UrlMapping;
import com.origin.demo.repository.UrlMappingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UrlShortenerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UrlMappingRepository urlMappingRepository;

    @Autowired
    private ObjectMapper mapper;

    @BeforeEach
    void cleanup() {
        urlMappingRepository.deleteAll();
    }

    @Test
    void whenPostValidUrlThenReturnsShortUrl() throws Exception {
        ShortenRequest req = new ShortenRequest();
        req.setUrl("https://spring.io");

        mockMvc.perform(post("/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortUrl").isString())
                .andExpect(jsonPath("$.shortUrl").value(startsWith("http://localhost:8080/")));
    }

    @Test
    void givenExistingCodeThenGetCodeThenRedirects() throws Exception {
        urlMappingRepository.save(UrlMapping.builder()
                .shortCode("abc123")
                .originalUrl("https://spring.io")
                .build());

        mockMvc.perform(get("/abc123"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://spring.io"));
    }

    @Test
    void givenExistingCodeWhenGetInfoThenReturnMapping() throws Exception {
        urlMappingRepository.save(UrlMapping.builder()
                .shortCode("xyz789")
                .originalUrl("https://example.com")
                .build());

        mockMvc.perform(get("/info/xyz789"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortCode").value("xyz789"))
                .andExpect(jsonPath("$.originalUrl").value("https://example.com"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }
}
