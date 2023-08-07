package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto getItemById(Long id, Long ownerId);

    List<ItemDto> findAllItems(Long userId, int from, int size);

    ItemDto createItem(ItemDto itemDto, Long userId);

    ItemDto updateItem(ItemDto itemDto, Long id, Long userId);

    void deleteItem(Long id);

    List<ItemDto> search(String text, int from, int size);

    CommentDto createComment(Long itemId, Long userId, CommentDto commentDto);
}
