package ru.practicum.shareit.booking;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.State;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.validator.ValuesAllowedConstraint;

import javax.validation.Valid;
import java.time.LocalDateTime;

import static ru.practicum.shareit.Constant.HEADER_USER_ID;

@RestController
@RequestMapping(path = "/bookings")
@Validated
public class BookingController {
    private final BookingClient bookingClient;

    public BookingController(BookingClient bookingClient) {
        this.bookingClient = bookingClient;
    }

    @PostMapping
    public ResponseEntity<Object> createBooking(@Valid @RequestBody BookItemRequestDto bookItemRequestDto,
                                                @RequestHeader(HEADER_USER_ID) Long userId) {
        LocalDateTime start = bookItemRequestDto.getStart();
        LocalDateTime end = bookItemRequestDto.getEnd();

        if (!start.isBefore(end)) {
            throw new BadRequestException("Дата начала должна быть раньше даты завершения");
        }

        return bookingClient.createBooking(bookItemRequestDto, userId);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> responseByOwner(@PathVariable Long bookingId,
                                                  @RequestHeader(HEADER_USER_ID) Long userId,
                                                  @RequestParam Boolean approved) {
        return bookingClient.responseByOwner(bookingId, userId, approved);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getById(@PathVariable Long bookingId,
                                          @RequestHeader(HEADER_USER_ID) Long userId) {
        return bookingClient.getBookingById(bookingId, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllByUser(@RequestHeader(HEADER_USER_ID) Long userId,
                                               @ValuesAllowedConstraint(propName = "state",
                                                       values = {"all",
                                                               "current",
                                                               "past",
                                                               "future",
                                                               "waiting",
                                                               "rejected"},
                                                       message = "Unknown state: UNSUPPORTED_STATUS")
                                               @RequestParam(defaultValue = "all") String state,
                                               @RequestParam(defaultValue = "0") int from,
                                               @RequestParam(defaultValue = "20") int size) {
        if (from < 0 || size <= 0) {
            throw new BadRequestException("Некорректный запрос.");
        }

        return bookingClient.getAllBookingsByUserId(userId, State.valueOf(state.toUpperCase()), from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getAllByOwner(@RequestHeader(HEADER_USER_ID) Long userId,
                                                @ValuesAllowedConstraint(propName = "state",
                                                        values = {"all",
                                                                "current",
                                                                "past",
                                                                "future",
                                                                "waiting",
                                                                "rejected"},
                                                        message = "Unknown state: UNSUPPORTED_STATUS")
                                                @RequestParam(defaultValue = "all") String state,
                                                @RequestParam(defaultValue = "0") int from,
                                                @RequestParam(defaultValue = "20") int size) {
        if (from < 0 || size <= 0) {
            throw new BadRequestException("Некорректный запрос.");
        }
        return bookingClient.getAllBookingsByOwnerId(userId, State.valueOf(state.toUpperCase()), from, size);
    }
}
