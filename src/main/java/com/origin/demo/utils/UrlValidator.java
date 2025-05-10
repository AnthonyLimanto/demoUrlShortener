package com.origin.demo.utils;

import java.net.MalformedURLException;
import java.net.URL;

public class UrlValidator {
    public static boolean isValid(String url) {
        try {
            new URL(url);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }
}
