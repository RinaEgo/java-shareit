package ru.practicum.shareit.item.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.CommentMapper;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository itemRequestRepository;
    private final ItemMapper itemMapper = new ItemMapper();
    private final CommentMapper commentMapper = new CommentMapper();
    private final BookingMapper bookingMapper = new BookingMapper();

    @Autowired
    public ItemServiceImpl(ItemRepository itemRepository,
                           UserRepository userRepository,
                           BookingRepository bookingRepository,
                           CommentRepository commentRepository,
                           ItemRequestRepository itemRequestRepository) {

        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.commentRepository = commentRepository;
        this.itemRequestRepository = itemRequestRepository;
    }

    @Transactional(readOnly = true)
    @Override
    public ItemDto getItemById(Long id, Long ownerId) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Предмет с id " + id + " не найден."));

        ItemDto itemDto = itemMapper.toItemDto(item);

        itemDto.setComments(commentRepository.findAllByItemId(id)
                .stream().map(commentMapper::toCommentDto).collect(Collectors.toList()));

        if (item.getOwner().getId().equals(ownerId)) {
            addBookingInfoForItemOwner(itemDto);
        }

        return itemDto;
    }

    @Transactional(readOnly = true)
    @Override
    public List<ItemDto> findAllItems(Long userId, int from, int size) {

        List<ItemDto> itemDtoList = itemRepository
                .findAllByOwnerId(userId, PageRequest.of(from / size, size, Sort.by("id")))
                .stream()
                .map(itemMapper::toItemDto)
                .collect(Collectors.toList());

        itemDtoList.forEach(itemDto -> {
            addBookingInfoForItemOwner(itemDto);

            itemDto.setComments(commentRepository.findAllByItemId(itemDto.getId())
                    .stream().map(commentMapper::toCommentDto).collect(Collectors.toList()));
        });

        return itemDtoList;
    }

    private ItemDto addBookingInfoForItemOwner(ItemDto itemDtoWithDate) {
        Booking lastBooking = bookingRepository.findBookingByItemWithDateBefore(itemDtoWithDate.getId(),
                LocalDateTime.now());
        Booking nextBooking = bookingRepository.findBookingByItemWithDateAfter(itemDtoWithDate.getId(),
                LocalDateTime.now());
        if (lastBooking != null && lastBooking.getStatus().equals(Status.APPROVED)) {
            itemDtoWithDate.setLastBooking(bookingMapper.toBookingCreationDto(lastBooking));
        }
        if (nextBooking != null && !nextBooking.getStatus().equals(Status.REJECTED)) {
            itemDtoWithDate.setNextBooking(bookingMapper.toBookingCreationDto(nextBooking));
        }
        return itemDtoWithDate;
    }

    @Transactional
    @Override
    public ItemDto createItem(ItemDto itemDto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден."));

        Item item = itemMapper.toItem(itemDto);
        item.setOwner(user);

        if (itemDto.getRequestId() != null) {
            ItemRequest itemRequest = itemRequestRepository.findById(itemDto.getRequestId())
                    .orElseThrow(() -> new NotFoundException("Запрос с ID " + itemDto.getRequestId() + " не найден."));
            item.setRequest(itemRequest);
        }

        itemRepository.save(item);

        return itemMapper.toItemDto(item);
    }

    @Transactional
    @Override
    public ItemDto updateItem(ItemDto itemDto, Long id, Long userId) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Предмет с id " + id + " не найден."));

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

        return itemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    public void deleteItem(Long id) {
        itemRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ItemDto> search(String text, int from, int size) {
        List<ItemDto> searchResult = new ArrayList<>();

        if (text.isEmpty() || text.isBlank()) {
            return searchResult;
        }

        Pageable pageable = PageRequest.of(from / size, size, Sort.by(Sort.Direction.ASC, "id"));

        searchResult = itemRepository.search(text, pageable)
                .stream()
                .map(itemMapper::toItemDto)
                .collect(Collectors.toList());

        return searchResult;
    }

    @Override
    public CommentDto createComment(Long itemId, Long userId, CommentDto commentDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден."));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Предмет с id " + itemId + " не найден."));

        if (bookingRepository.findAllByBookerIdAndItemIdAndStatusEqualsAndEndIsBefore(userId, itemId, Status.APPROVED,
                LocalDateTime.now()).isEmpty()) {
            throw new ValidationException("Предмет не бронировался пользователем с id " + userId +
                    " или аренда не завершена. Доступ к комментированию предмета закрыт.");
        }

        Comment comment = commentMapper.toComment(commentDto);
        comment.setItem(item);
        comment.setAuthor(user);
        comment.setCreated(LocalDateTime.now());
        commentRepository.save(comment);

        return commentMapper.toCommentDto(comment);
    }
}
