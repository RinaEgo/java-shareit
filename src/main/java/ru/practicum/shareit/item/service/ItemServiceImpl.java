package ru.practicum.shareit.item.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.ArrayList;
import java.util.List;

@Service
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;

    @Autowired
    public ItemServiceImpl(ItemStorage itemStorage, UserStorage userStorage) {
        this.itemStorage = itemStorage;
        this.userStorage = userStorage;
    }

    ItemMapper mapper = new ItemMapper();

    public void validateItem(Integer id) {
        if (!itemStorage.getItemsMap().containsKey(id)) {
            throw new NotFoundException("Предмет с ID " + id + " не найден.");
        }
    }

    public void validateUser(Integer id) {
        if (!userStorage.getUsersMap().containsKey(id)) {
            throw new NotFoundException("Пользователь с ID " + id + " не найден.");
        }
    }

    @Override
    public ItemDto getItemById(Integer id) {
        validateItem(id);

        Item item = itemStorage.getItemById(id);

        return mapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> findAllItems(Integer userId) {
        List<ItemDto> items = new ArrayList<>();

        for (Item item : itemStorage.findAllItems()) {
            if (item.getOwner().getId().equals(userId)) {
                items.add(mapper.toItemDto(item));
            }
        }
        return items;
    }

    @Override
    public ItemDto createItem(ItemDto itemDto, Integer userId) {
        validateUser(userId);
        User user = userStorage.getUserById(userId);

        Item item = mapper.toItem(itemDto);
        item.setOwner(user);
        itemStorage.createItem(item);

        return mapper.toItemDto(item);

    }

    @Override
    public ItemDto updateItem(ItemDto itemDto, Integer id, Integer userId) {
        validateItem(id);
        Item item = itemStorage.getItemById(id);

        if (!item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Пользователь с id: " + userId + "не является владельцем предмета.");
        }

        if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }

        return mapper.toItemDto(itemStorage.updateItem(item));
    }

    @Override
    public void deleteItem(Integer id) {
        validateItem(id);

        itemStorage.deleteItem(id);
    }

    @Override
    public List<ItemDto> search(String text) {
        List<ItemDto> searchResult = new ArrayList<>();

        if (text.isEmpty() || text.isBlank()) {
            return searchResult;
        }

        for (Item item : itemStorage.findAllItems()) {
            if (doesExist(text, item)) {
                searchResult.add(mapper.toItemDto(item));
            }
        }
        return searchResult;
    }

    private Boolean doesExist(String text, Item item) {
        return (item.getName().toUpperCase().contains(text.toUpperCase()) ||
                item.getDescription().toUpperCase().contains(text.toUpperCase())) && item.getAvailable();
    }
}
