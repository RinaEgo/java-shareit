package ru.practicum.shareit.item.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class InMemoryItemStorage implements ItemStorage {
    private final Map<Integer, Item> items = new HashMap<>();
    private int itemId = 1;

    @Override
    public Map<Integer, Item> getItemsMap() {
        return items;
    }

    @Override
    public Item getItemById(Integer id) {
        if (items.containsKey(id)) {
            return items.get(id);
        } else {
            log.warn("Предмет не существует.");
            throw new NotFoundException("Предмет не существует.");
        }
    }

    @Override
    public List<Item> findAllItems() {
        return new ArrayList<>(items.values());
    }

    @Override
    public Item createItem(Item item) {
        if (items.containsKey(item.getId())) {
            log.warn("Предмет уже существует.");
            throw new ValidationException("Предмет уже существует.");
        } else {
            item.setId(itemId);
            items.put(item.getId(), item);
            itemId++;
            log.info("Предмет {} добавлен.", item);
        }
        return item;
    }

    @Override
    public Item updateItem(Item item) {
        if (items.containsKey(item.getId())) {
            items.put(item.getId(), item);
            log.info("Предмет {} добавлен.", item);
        } else {
            log.warn("Предмет не существует.");
            throw new NotFoundException("Предмет не существует.");
        }
        return item;
    }

    @Override
    public void deleteItem(Integer id) {
        if (items.containsKey(id)) {
            log.info("Предмет {} удален.", getItemById(id));
            items.remove(id);
        } else {
            log.warn("Предмет не существует.");
            throw new NotFoundException("Предмет не существует.");
        }
    }
}
