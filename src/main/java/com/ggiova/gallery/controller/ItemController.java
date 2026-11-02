package com.ggiova.gallery.controller;

import com.ggiova.gallery.dto.ItemUpdate;
import com.ggiova.gallery.dto.ItemListing;
import com.ggiova.gallery.dto.UploadResult;
import com.ggiova.gallery.service.ItemService;
import com.ggiova.gallery.service.VariantService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
public class ItemController {
    private final ItemService itemService;
    private final VariantService variantService;

    public ItemController(
        ItemService itemService,
        VariantService variantService
    ) {
        this.itemService    = itemService;
        this.variantService = variantService;
    }

    @GetMapping
    public List<ItemListing> findAll() {
        return this.itemService.listAll();
    }

    @PostMapping("/upload")
    public List<UploadResult> create(@RequestParam MultipartFile[] files) {
        return Arrays.stream(files)
                     .map(this.itemService::create)
                     .toList();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<String> review(@PathVariable long id) {
        return this.itemService.review(id).toResponseEntity();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, String>> update(@PathVariable long id, @RequestBody ItemUpdate item) {
        return this.itemService.update(id, item)
                               .map(ResponseEntity::ok)
                               .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable long id) {
        return this.itemService.deleteById(id)
                               ? ResponseEntity.noContent().build()
                               : ResponseEntity.notFound().build();
    }
}
