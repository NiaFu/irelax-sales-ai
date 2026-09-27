package com.irelax.salesai.ai;

import com.irelax.salesai.domain.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class RuleBasedSalesAiAssistant {

    public SalesAiResult analyseAndDraft(Customer customer, Message inbound, List<Product> products) {
        String text = inbound.getContent().toLowerCase(Locale.ROOT);
        String intent = "GENERAL_ENQUIRY";
        if (containsAny(text, "price", "deal", "discount", "cash", "best price", "cheaper")) intent = "PRICE_NEGOTIATION";
        else if (containsAny(text, "warranty", "guarantee")) intent = "WARRANTY";
        else if (containsAny(text, "delivery", "deliver", "install")) intent = "DELIVERY";
        else if (containsAny(text, "problem", "broken", "repair", "not working")) intent = "AFTER_SALES";

        List<String> detected = new ArrayList<>();
        for (Product product : products) {
            if (text.contains(product.getName().toLowerCase(Locale.ROOT))) detected.add(product.getName());
        }

        String firstName = customer.getFirstName() == null || customer.getFirstName().equalsIgnoreCase("New lead") ? "" : " " + customer.getFirstName();
        String draft = "Hi" + firstName + ", thanks for your message. I’ll check this for you and get back to you with the correct details.";
        if (intent.equals("PRICE_NEGOTIATION")) {
            draft = "Hi" + firstName + ", thanks for checking. I’ll look at the current options for you and make sure I give you the correct price.";
        }
        boolean followUp = !intent.equals("AFTER_SALES");
        SalesStage stage = intent.equals("PRICE_NEGOTIATION") ? SalesStage.HOT : null;
        return new SalesAiResult(draft, intent, "NEUTRAL", detected, followUp, followUp ? 3 : null, stage,
                "Local fallback analysis. Configure OPENAI_API_KEY for richer drafts.", "rule-based-fallback");
    }

    private boolean containsAny(String value, String... terms) {
        for (String term : terms) if (value.contains(term)) return true;
        return false;
    }
}
