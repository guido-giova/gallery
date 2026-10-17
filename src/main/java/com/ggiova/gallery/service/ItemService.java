package com.ggiova.gallery.service;

import com.ggiova.gallery.data.Item;
import com.ggiova.gallery.mapper.ItemMapper;
import com.ggiova.gallery.repository.ItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ItemService {
    private final ItemRepository itemRepository;

    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public Item create(Item item) {
        return itemRepository.insert(item);
    }

    public Optional<Item> findById(long id) {
        return itemRepository.findById(id);
    }

    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    public Optional<Item> update(long id, Item item) {
        Item itemWithPathId = ItemMapper.copyWithId(item, id);
        return itemRepository.update(itemWithPathId)
                ? Optional.of(itemWithPathId)
                : Optional.empty();
    }

    public boolean deleteById(long id) {
        return itemRepository.deleteById(id);
    }
}
