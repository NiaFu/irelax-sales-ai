package com.irelax.salesai.api;

import com.irelax.salesai.api.dto.ProductApi;
import com.irelax.salesai.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService service;

    public ProductController(ProductService service) { this.service = service; }

    @GetMapping
    public List<ProductApi.Response> list(@RequestParam(defaultValue = "false") boolean includeInactive) {
        return service.list(includeInactive);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductApi.Response create(@Valid @RequestBody ProductApi.UpsertRequest request) { return service.create(request); }

    @PutMapping("/{id}")
    public ProductApi.Response update(@PathVariable UUID id, @Valid @RequestBody ProductApi.UpsertRequest request) {
        return service.update(id, request);
    }
}
