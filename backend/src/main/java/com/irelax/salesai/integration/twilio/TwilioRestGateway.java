package com.irelax.salesai.integration.twilio;

import tools.jackson.databind.JsonNode;
import com.irelax.salesai.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
public class TwilioRestGateway implements TwilioGateway {
    private static final Logger log = LoggerFactory.getLogger(TwilioRestGateway.class);
    private final AppProperties properties;
    private final RestClient.Builder restClientBuilder;

    public TwilioRestGateway(AppProperties properties, RestClient.Builder restClientBuilder) {
        this.properties = properties;
        this.restClientBuilder = restClientBuilder;
    }

    @Override
    public SendResult sendSms(String to, String body) {
        AppProperties.Twilio twilio = properties.twilio();
        if (twilio == null) throw new IllegalStateException("Twilio configuration is missing");
        if (twilio.mockEnabled()) {
            String sid = "SM_DEV_" + UUID.randomUUID().toString().replace("-", "");
            log.info("Mock SMS to {}: {}", to, body);
            return new SendResult(sid, "sent");
        }
        if (isBlank(twilio.accountSid()) || isBlank(twilio.authToken()) || isBlank(twilio.phoneNumber())) {
            throw new IllegalStateException("Twilio credentials and phone number are required when mock mode is disabled");
        }

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("To", to);
        form.add("From", twilio.phoneNumber());
        form.add("Body", body);
        if (!isBlank(twilio.statusCallbackUrl())) form.add("StatusCallback", twilio.statusCallbackUrl());

        RestClient client = restClientBuilder
                .baseUrl("https://api.twilio.com/2010-04-01")
                .defaultHeaders(headers -> headers.setBasicAuth(twilio.accountSid(), twilio.authToken()))
                .build();
        JsonNode response = client.post()
                .uri("/Accounts/{sid}/Messages.json", twilio.accountSid())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(JsonNode.class);
        if (response == null || response.path("sid").asText().isBlank()) {
            throw new IllegalStateException("Twilio did not return a message SID");
        }
        return new SendResult(response.path("sid").asText(), response.path("status").asText("queued"));
    }

    private boolean isBlank(String value) { return value == null || value.isBlank(); }
}
