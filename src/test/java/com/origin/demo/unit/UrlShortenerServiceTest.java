package com.origin.demo.unit;

import com.origin.demo.exception.NotFoundException;
import com.origin.demo.model.UrlMapping;
import com.origin.demo.repository.UrlMappingRepository;
import com.origin.demo.service.UrlShortenerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UrlShortenerServiceTest {

    @Mock
    UrlMappingRepository urlMappingRepository;

    @InjectMocks
    UrlShortenerService urlShortenerService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(urlShortenerService, "baseUrl", "http://test");
    }

    @Test
    void shortenUrlSaveToRepoAndReturnsShortUrl() {
        when(urlMappingRepository.save(any(UrlMapping.class)))
            .thenAnswer(returnsFirstArg());

        String result = urlShortenerService.getShortUrl("https://foo.com");

        assertThat(result).startsWith("http://test");
        verify(urlMappingRepository,times(1)).save(any(UrlMapping.class));
    }

    @Test
    void shortenUrlCollisionThenRetrySuccess() {
        when(urlMappingRepository.save(any(UrlMapping.class)))
            .thenThrow(DataIntegrityViolationException.class)
            .thenAnswer(returnsFirstArg());

        String result = urlShortenerService.getShortUrl("https://foo.com");

        assertThat(result).startsWith("http://test");
        verify(urlMappingRepository,times(2)).save(any(UrlMapping.class));
    }

    @Test
    void getUrlMappingNotFound() {
        when(urlMappingRepository.findByShortCode("abc"))
            .thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> urlShortenerService.getUrlMapping("abc")
        );
        assertEquals("Short Url not found", ex.getMessage());
    }

}
