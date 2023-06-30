package ru.practicum.shareit.item.storage;

import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Map;

public interface ItemStorage {

    Map<Integer, Item> getItemsMap();

    Item getItemById(Integer id);

    List<Item> findAllItems();

    Item createItem(Item item);

    Item updateItem(Item item);

    void deleteItem(Integer id);

}
