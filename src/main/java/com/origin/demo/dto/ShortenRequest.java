package com.origin.demo.dto;

import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class ShortenRequest {
    private String url;
}
