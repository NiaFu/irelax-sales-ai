package com.irelax.salesai.integration.twilio;

import com.irelax.salesai.config.AppProperties;
import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.assertj.core.api.Assertions.assertThat;

class TwilioSignatureValidatorTest {
    @Test
    void acceptsMatchingSignatureAndRejectsTampering() throws Exception {
        String token = "test-token";
        String url = "https://example.com/api/webhooks/twilio/sms";
        Map<String, String> params = Map.of("From", "+61411111111", "To", "+61422222222", "Body", "Hello");
        String signature = sign(url, params, token);
        AppProperties properties = new AppProperties(
                new AppProperties.Auth(false, "", "nia"),
                new AppProperties.Twilio("sid", token, "+61422222222", "", "", "nia", false),
                new AppProperties.OpenAi("", "gpt-6-luna", "https://api.openai.com"),
                new AppProperties.Cors(List.of("http://localhost:8081")));
        TwilioSignatureValidator validator = new TwilioSignatureValidator(properties);

        assertThat(validator.validate(signature, url, params, token)).isTrue();
        assertThat(validator.validate(signature, url, Map.of("Body", "Tampered"), token)).isFalse();
    }

    private String sign(String url, Map<String, String> params, String token) throws Exception {
        StringBuilder data = new StringBuilder(url);
        new TreeMap<>(params).forEach((key, value) -> data.append(key).append(value));
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(new SecretKeySpec(token.getBytes(StandardCharsets.UTF_8), "HmacSHA1"));
        return Base64.getEncoder().encodeToString(mac.doFinal(data.toString().getBytes(StandardCharsets.UTF_8)));
    }
}
