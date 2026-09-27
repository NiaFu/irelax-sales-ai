package com.irelax.salesai.integration.twilio;

import com.irelax.salesai.config.AppProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Map;
import java.util.TreeMap;

@Component
public class TwilioSignatureValidator {
    private final AppProperties properties;

    public TwilioSignatureValidator(AppProperties properties) {
        this.properties = properties;
    }

    public boolean validate(HttpServletRequest request, MultiValueMap<String, String> params, String signature) {
        if (properties.twilio() == null || properties.twilio().authToken() == null || properties.twilio().authToken().isBlank()) {
            return properties.twilio() != null && properties.twilio().mockEnabled();
        }
        String url = externalUrl(request);
        Map<String, String> flattened = new TreeMap<>();
        params.forEach((key, values) -> {
            if (values != null && !values.isEmpty()) flattened.put(key, values.getFirst());
        });
        return validate(signature, url, flattened, properties.twilio().authToken());
    }

    boolean validate(String signature, String url, Map<String, String> params, String authToken) {
        if (signature == null || signature.isBlank()) return false;
        StringBuilder data = new StringBuilder(url);
        new TreeMap<>(params).forEach((key, value) -> data.append(key).append(value == null ? "" : value));
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(authToken.getBytes(StandardCharsets.UTF_8), "HmacSHA1"));
            String expected = Base64.getEncoder().encodeToString(mac.doFinal(data.toString().getBytes(StandardCharsets.UTF_8)));
            return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), signature.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException("Unable to validate Twilio signature", e);
        }
    }

    private String externalUrl(HttpServletRequest request) {
        String base = properties.twilio() == null ? null : properties.twilio().webhookBaseUrl();
        if (base != null && !base.isBlank()) {
            return base.replaceAll("/$", "") + request.getRequestURI();
        }
        return request.getRequestURL().toString();
    }
}
