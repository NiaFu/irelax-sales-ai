package com.irelax.salesai.api;

import com.irelax.salesai.auth.OwnerContext;
import com.irelax.salesai.domain.Message;
import com.irelax.salesai.integration.twilio.TwilioSignatureValidator;
import com.irelax.salesai.service.AiProcessingService;
import com.irelax.salesai.service.MessagingService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/webhooks/twilio")
public class TwilioWebhookController {
    private final TwilioSignatureValidator validator;
    private final MessagingService messaging;
    private final AiProcessingService ai;
    private final OwnerContext ownerContext;

    public TwilioWebhookController(TwilioSignatureValidator validator, MessagingService messaging,
                                   AiProcessingService ai, OwnerContext ownerContext) {
        this.validator = validator;
        this.messaging = messaging;
        this.ai = ai;
        this.ownerContext = ownerContext;
    }

    @PostMapping(value = "/sms", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_XML_VALUE)
    public String inbound(@RequestHeader(value = "X-Twilio-Signature", required = false) String signature,
                          @RequestParam MultiValueMap<String, String> params,
                          HttpServletRequest request) {
        requireValidSignature(request, params, signature);
        String from = require(params, "From");
        String to = require(params, "To");
        String body = params.getFirst("Body");
        String sid = params.getFirst("MessageSid");
        Message message = messaging.receiveInbound(ownerContext.webhookOwnerId(), from, to, body, sid);
        ai.processInboundAsync(message.getId());
        return "<Response></Response>";
    }

    @PostMapping(value = "/status", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void status(@RequestHeader(value = "X-Twilio-Signature", required = false) String signature,
                       @RequestParam MultiValueMap<String, String> params,
                       HttpServletRequest request) {
        requireValidSignature(request, params, signature);
        messaging.updateDeliveryStatus(params.getFirst("MessageSid"), params.getFirst("MessageStatus"), params.getFirst("ErrorCode"));
    }

    private void requireValidSignature(HttpServletRequest request, MultiValueMap<String, String> params, String signature) {
        if (!validator.validate(request, params, signature)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid Twilio signature");
        }
    }

    private String require(MultiValueMap<String, String> params, String key) {
        String value = params.getFirst(key);
        if (value == null || value.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, key + " is required");
        return value;
    }
}
