package com.irelax.salesai.api;

import com.irelax.salesai.api.dto.AssistantApi;
import com.irelax.salesai.service.SalesAssistantQueryService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assistant")
public class AssistantController {
    private final SalesAssistantQueryService service;
    public AssistantController(SalesAssistantQueryService service) { this.service = service; }

    @PostMapping("/query")
    public AssistantApi.QueryResponse query(@Valid @RequestBody AssistantApi.QueryRequest request) {
        return service.query(request.query());
    }
}
