package com.ggiova.gallery.controller;

import com.ggiova.gallery.dto.VariantListing;
import com.ggiova.gallery.service.ItemService;
import com.ggiova.gallery.service.VariantService;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/files")
public class VariantController {
    private final VariantService variantService;

    public VariantController(
        VariantService variantService
    ) {
        this.variantService = variantService;
    }

    @GetMapping("/{id}")
    public List<VariantListing> getVariants(@PathVariable long id) {
        return this.variantService.listAll(id)
                                  .stream()
                                  .map(v -> new VariantListing(v.id(), v.kind(), v.mime()))
                                  .toList();
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
