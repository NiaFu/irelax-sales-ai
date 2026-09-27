package com.irelax.salesai.integration.twilio;

public interface TwilioGateway {
    SendResult sendSms(String to, String body);
    record SendResult(String sid, String status) {}
}
