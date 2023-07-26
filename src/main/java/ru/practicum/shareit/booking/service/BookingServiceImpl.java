package ru.practicum.shareit.booking.service;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.booking.dto.BookingCreationDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    public BookingServiceImpl(BookingRepository bookingRepository, UserRepository userRepository,
                              ItemRepository itemRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
    }

    BookingMapper mapper = new BookingMapper();
    private final Sort sort = Sort.by(Sort.Direction.DESC, "start");

    @Transactional
    @Override
    public BookingDto createBooking(BookingCreationDto bookingCreationDto, Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(("Пользователь с ID " + userId + " не найден.")));

        Item item = itemRepository.findById(bookingCreationDto.getItemId())
                .orElseThrow(() -> new NotFoundException("Предмет с ID " + bookingCreationDto.getItemId() + " не найден."));

        if (item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Предмет принадлежит пользователю. Бронирование невозможно.");
        }

        if (!item.getAvailable()) {
            throw new BadRequestException("Предмет недоступен. Бронирование невозможно.");
        }

        Booking booking = mapper.toBooking(bookingCreationDto);
        if (booking.getEnd().isBefore(booking.getStart()) || booking.getStart().equals(booking.getEnd())) {
            throw new BadRequestException("Дата окончания бронирования раньше или равно дате начала. Бронирование невозможно.");
        }

        booking.setBooker(user);
        booking.setItem(item);
        booking.setStatus(Status.WAITING);
        bookingRepository.save(booking);

        return mapper.toBookingDto(booking);
    }

    @Transactional
    @Override
    public BookingDto responseByOwner(Long bookingId, Long userId, Boolean approved) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование с id " + bookingId + "не найдено."));

        if (!userId.equals(booking.getItem().getOwner().getId())) {
            throw new NotFoundException("Бронирование не принадлежит пользователю с id " + userId);
        }

        if (!booking.getStatus().equals(Status.WAITING)) {
            throw new BadRequestException("Статус ожидания уже был изменен владельцем.");
        }

        if (approved) {
            booking.setStatus(Status.APPROVED);
        } else {
            booking.setStatus(Status.REJECTED);
        }

        bookingRepository.save(booking);

        return mapper.toBookingDto(booking);
    }

    @Transactional(readOnly = true)
    @Override
    public BookingDto getBookingById(Long bookingId, Long userId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование с id " + bookingId + " не найдено."));

        if (!userId.equals(booking.getBooker().getId()) && !userId.equals(booking.getItem().getOwner().getId())) {
            throw new NotFoundException("Доступ невозможен. " +
                    "Пользователь не является хозяином вещи или автором брони.");
        }

        return mapper.toBookingDto(booking);
    }

    @Transactional(readOnly = true)
    @Override
    public List<BookingDto> getAllBookingsByUserId(Long userId, String state) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден."));

        List<Booking> bookings = new ArrayList<>();

        switch (state) {
            case "ALL":
                bookings.addAll(bookingRepository.findAllByBooker(user, sort));
                break;
            case "CURRENT":
                bookings.addAll(bookingRepository.findAllByBookerAndStartBeforeAndEndAfter(user,
                        LocalDateTime.now(), LocalDateTime.now(), sort));
                break;
            case "PAST":
                bookings.addAll(bookingRepository.findAllByBookerAndEndBefore(user,
                        LocalDateTime.now(), sort));
                break;
            case "FUTURE":
                bookings.addAll(bookingRepository.findAllByBookerAndStartAfter(user, LocalDateTime.now(), sort));
                break;
            case "WAITING":
                bookings.addAll(bookingRepository.findAllByBookerAndStatusEquals(user, Status.WAITING, sort));
                break;
            case "REJECTED":
                bookings.addAll(bookingRepository.findAllByBookerAndStatusEquals(user, Status.REJECTED, sort));
                break;
            default:
                throw new ValidationException("Unknown state: UNSUPPORTED_STATUS");
        }

        return bookings.stream().map(mapper::toBookingDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<BookingDto> getAllBookingsByOwnerId(Long userId, String state) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден."));

        List<Booking> bookings = new ArrayList<>();

        switch (state) {
            case "ALL":
                bookings.addAll(bookingRepository.findAllByItemOwner(user, sort));
                break;

            case "CURRENT":
                bookings.addAll(bookingRepository.findAllByItemOwnerAndStartBeforeAndEndAfter(user,
                        LocalDateTime.now(), LocalDateTime.now(), sort));
                break;

            case "PAST":
                bookings.addAll(bookingRepository.findAllByItemOwnerAndEndBefore(user,
                        LocalDateTime.now(), sort));
                break;

            case "FUTURE":
                bookings.addAll(bookingRepository.findAllByItemOwnerAndStartAfter(user, LocalDateTime.now(), sort));
                break;

            case "WAITING":
                bookings.addAll(bookingRepository.findAllByItemOwnerAndStatusEquals(user, Status.WAITING, sort));
                break;

            case "REJECTED":
                bookings.addAll(bookingRepository.findAllByItemOwnerAndStatusEquals(user, Status.REJECTED, sort));
                break;

            default:
                throw new ValidationException("Unknown state: UNSUPPORTED_STATUS");
        }
        return bookings.stream().map(mapper::toBookingDto).collect(Collectors.toList());
    }
}
