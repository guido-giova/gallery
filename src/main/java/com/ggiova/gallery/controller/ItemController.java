package com.ggiova.gallery.controller;

import com.ggiova.gallery.data.Item;
import com.ggiova.gallery.dto.ItemListing;
import com.ggiova.gallery.dto.UploadResult;
import com.ggiova.gallery.service.ItemService;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = {"${app.cors.allowed}"})
public class ItemController {
    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping
    public List<ItemListing> findAll() {
        return this.itemService.listAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Item> findById(@PathVariable long id) {
        return itemService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/content")
    public ResponseEntity<Resource> getContent(@PathVariable long id) {
        return this.itemService.getContent(id)
                               .map(c -> ResponseEntity.ok()
                                       .contentType(MediaType.parseMediaType(c.mime()))
                                       .body(c.resource())
                               ).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/upload")
    public List<UploadResult> create(@RequestParam MultipartFile[] files) {
        return Arrays.stream(files)
                     .map(this.itemService::create)
                     .toList();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Item> update(@PathVariable long id, @RequestBody Item item) {
        return itemService.update(id, item)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable long id) {
        return itemService.deleteById(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
