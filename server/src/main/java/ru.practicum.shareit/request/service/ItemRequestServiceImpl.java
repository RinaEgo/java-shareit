package ru.practicum.shareit.request.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.ItemRequestMapper;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemRequestMapper requestMapper = new ItemRequestMapper();
    private final ItemMapper itemMapper = new ItemMapper();
    private final Sort sort = Sort.by(Sort.Direction.ASC, "created");

    @Autowired
    public ItemRequestServiceImpl(ItemRequestRepository itemRequestRepository,
                                  UserRepository userRepository,
                                  ItemRepository itemRepository) {
        this.itemRequestRepository = itemRequestRepository;
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
    }


    @Transactional
    @Override
    public ItemRequestDto createRequest(Long userId, ItemRequestDto itemRequestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден."));

        ItemRequest itemRequest = requestMapper.toItemRequest(itemRequestDto);

        itemRequest.setCreated(LocalDateTime.now());
        itemRequest.setRequestor(user);

        itemRequestRepository.save(itemRequest);

        return requestMapper.toItemRequestDto(itemRequest);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ItemRequestDto> findAllRequestsByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден."));

        List<ItemRequestDto> itemRequestDtoList = itemRequestRepository.findAllByRequestorIdOrderByCreatedAsc(userId)
                .stream()
                .map(requestMapper::toItemRequestDto)
                .collect(Collectors.toList());

        for (ItemRequestDto itemRequestDto : itemRequestDtoList) {
            itemRequestDto.setItems(itemRepository.findAllByRequestId(itemRequestDto.getId())
                    .stream()
                    .map(itemMapper::toItemShortDto)
                    .collect(Collectors.toList()));
        }
        return itemRequestDtoList;
    }

    @Transactional(readOnly = true)
    @Override
    public List<ItemRequestDto> findAllRequests(Long userId, int from, int size) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден."));

        List<ItemRequestDto> itemRequestDtoList = itemRequestRepository.findAllByRequestorNot(user,
                        PageRequest.of(from / size, size, sort))
                .stream()
                .map(requestMapper::toItemRequestDto)
                .collect(Collectors.toList());

        for (ItemRequestDto itemRequestDto : itemRequestDtoList) {
            itemRequestDto.setItems(itemRepository.findAllByRequestId(itemRequestDto.getId())
                    .stream()
                    .map(itemMapper::toItemShortDto)
                    .collect(Collectors.toList()));
        }
        return itemRequestDtoList;
    }

    @Transactional(readOnly = true)
    @Override
    public ItemRequestDto getRequestById(Long requestId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден."));

        ItemRequest itemRequest = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос с ID " + requestId + " не найден."));

        ItemRequestDto itemRequestDto = requestMapper.toItemRequestDto(itemRequest);

        itemRequestDto.setItems(itemRepository.findAllByRequestId(itemRequestDto.getId())
                .stream()
                .map(itemMapper::toItemShortDto)
                .collect(Collectors.toList()));

        return itemRequestDto;
    }
}
