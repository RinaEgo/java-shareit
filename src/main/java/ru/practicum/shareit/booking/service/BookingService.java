package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingCreationDto;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.List;

public interface BookingService {

    BookingDto createBooking(BookingCreationDto bookingShortDto, Long userId);

    BookingDto responseByOwner(Long bookingId, Long userId, Boolean approved);

    List<BookingDto> getAllBookingsByOwnerId(Long userId, String state, int from, int size);

    List<BookingDto> getAllBookingsByUserId(Long userId, String state, int from, int size);

    BookingDto getBookingById(Long bookingId, Long userId);
}
