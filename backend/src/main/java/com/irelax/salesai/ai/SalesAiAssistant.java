package com.irelax.salesai.ai;

import com.irelax.salesai.domain.*;

import java.util.List;

public interface SalesAiAssistant {
    SalesAiResult analyseAndDraft(Customer customer,
                                  Message inbound,
                                  List<Message> recentMessages,
                                  List<Product> products,
                                  SalesAssistantProfile profile,
                                  List<KnowledgeEntry> knowledgeEntries);
}
