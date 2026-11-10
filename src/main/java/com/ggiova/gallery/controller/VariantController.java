package com.ggiova.gallery.controller;

import com.ggiova.gallery.service.VariantService;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/files")
public class VariantController {
    private final VariantService variantService;

    public VariantController(
        VariantService variantService
    ) {
        this.variantService = variantService;
    }

    @GetMapping("/{id}/content")
    public ResponseEntity<Resource> getContent(@PathVariable long id) {
        return this.variantService.getContent(id)
                               .map(c -> ResponseEntity.ok()
                                       .contentType(MediaType.parseMediaType(c.mime()))
                                       .body(c.resource())
                               ).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
