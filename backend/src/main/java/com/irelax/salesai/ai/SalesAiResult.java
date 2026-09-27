package com.irelax.salesai.ai;

import com.irelax.salesai.domain.SalesStage;

import java.util.List;

public record SalesAiResult(
        String draftReply,
        String intent,
        String sentiment,
        List<String> detectedProducts,
        boolean followUpRequired,
        Integer followUpDays,
        SalesStage stageSuggestion,
        String summary,
        String model
) {}
