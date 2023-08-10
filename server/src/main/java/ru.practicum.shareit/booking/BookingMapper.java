package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingCreationDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;

public class BookingMapper {

    public BookingDto toBookingDto(Booking booking) {
        return new BookingDto(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                booking.getItem(),
                booking.getBooker(),
                booking.getStatus());
    }

    public Booking toBooking(BookingCreationDto bookingCreationDto) {
        return new Booking(
                bookingCreationDto.getId(),
                bookingCreationDto.getStart(),
                bookingCreationDto.getEnd());
    }

    public BookingCreationDto toBookingCreationDto(Booking booking) {
        return new BookingCreationDto(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                booking.getItem().getId(),
                booking.getBooker().getId());
    }
}
