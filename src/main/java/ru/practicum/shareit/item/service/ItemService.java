package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto getItemById(Integer id);

    List<ItemDto> findAllItems(Integer userId);

    ItemDto createItem(ItemDto itemDto, Integer userId);

    ItemDto updateItem(ItemDto itemDto, Integer id, Integer userId);

    void deleteItem(Integer id);

    List<ItemDto> search(String text);

}
