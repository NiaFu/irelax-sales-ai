package com.irelax.salesai.ai;

import com.irelax.salesai.domain.Customer;
import com.irelax.salesai.domain.Message;
import com.irelax.salesai.domain.Product;
import com.irelax.salesai.domain.SalesStage;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RuleBasedSalesAiAssistantTest {
    private final RuleBasedSalesAiAssistant assistant = new RuleBasedSalesAiAssistant();

    @Test
    void detectsPriceNegotiationAndProduct() {
        Customer customer = new Customer();
        customer.setFirstName("Peter");
        Message inbound = new Message();
        inbound.setContent("Can you do a better price for ROBO?");
        Product product = new Product();
        product.setName("ROBO");

        SalesAiResult result = assistant.analyseAndDraft(customer, inbound, List.of(product));

        assertThat(result.intent()).isEqualTo("PRICE_NEGOTIATION");
        assertThat(result.detectedProducts()).containsExactly("ROBO");
        assertThat(result.stageSuggestion()).isEqualTo(SalesStage.HOT);
        assertThat(result.draftReply()).contains("Peter");
    }
}
