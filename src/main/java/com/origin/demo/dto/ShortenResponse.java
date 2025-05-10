package com.origin.demo.dto;

import com.origin.demo.model.UrlMapping;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ShortenResponse {
    private String shortUrl;
}
