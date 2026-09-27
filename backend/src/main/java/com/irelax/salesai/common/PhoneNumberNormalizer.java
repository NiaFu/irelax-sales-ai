package com.irelax.salesai.common;

import org.springframework.stereotype.Component;

@Component
public class PhoneNumberNormalizer {

    public String normalize(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Phone number is required");
        }
        String value = raw.trim().replaceAll("[^0-9+]", "");
        if (value.startsWith("+")) {
            return "+" + value.substring(1).replaceAll("\\D", "");
        }
        value = value.replaceAll("\\D", "");
        if (value.startsWith("04") && value.length() == 10) {
            return "+61" + value.substring(1);
        }
        if (value.startsWith("614") && value.length() == 11) {
            return "+" + value;
        }
        if (value.startsWith("61") && value.length() >= 10) {
            return "+" + value;
        }
        if (value.startsWith("0") && value.length() >= 9) {
            return "+61" + value.substring(1);
        }
        return "+" + value;
    }
}
