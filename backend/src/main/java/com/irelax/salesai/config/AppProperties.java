package com.irelax.salesai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        Auth auth,
        Twilio twilio,
        OpenAi openAi,
        Cors cors
) {
    public record Auth(boolean enabled, String issuerUri, String devOwnerId) {}

    public record Twilio(
            String accountSid,
            String authToken,
            String phoneNumber,
            String statusCallbackUrl,
            String webhookBaseUrl,
            String ownerId,
            boolean mockEnabled
    ) {}

    public record OpenAi(String apiKey, String model, String baseUrl) {}

    public record Cors(List<String> allowedOrigins) {}
}
